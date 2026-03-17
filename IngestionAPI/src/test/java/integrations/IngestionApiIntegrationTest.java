package integrations;

import com.spx.IngestionAPIApplication;
import com.spx.config.RabbitConfig;
import com.spx.dto.CrmIncomingCampaignDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(classes = IngestionAPIApplication.class)
@AutoConfigureMockMvc
@Import(RabbitConfig.class)
class IngestionApiIntegrationTest extends AbstractRabbitContainerTest {

    // ---- Test constants ----
    private static final String ENDPOINT = "/api/v1/crm/sync";
    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_KEY = "secret-key";
    private static final String INVALID_PAYLOAD = "{ invalid json }";
    private static final String QUEUE_NAME = "crm.campaign.queue";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setupRabbitTopology() {

        Queue queue = new Queue(QUEUE_NAME);
        DirectExchange exchange = new DirectExchange("crm.exchange");

        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareQueue(queue);

        rabbitAdmin.declareBinding(
                BindingBuilder.bind(queue)
                        .to(exchange)
                        .with("crm.campaign.created")
        );
    }

    @BeforeEach
    void cleanQueue() {
        rabbitAdmin.purgeQueue(QUEUE_NAME, true);
    }

    // MockMvc simulates HTTP requests (Get, Post...) without starting a real web server.
    @Autowired
    private MockMvc mockMvc;

    private static final String VALID_PAYLOAD =
            """
            [
                          {
                            "campaignId": "C-00088102",
                            "subCampaignId": "SC-0091",
                            "attendees": [
                              {
                                "cn": "1002001",
                                "firstName": "Matteo",
                                "lastName": "Ricci",
                                "birthDate": "1985-05-12",
                                "partnerId": "1002001",
                                "isCompanion": false,
                                "qrCode": "QR-1"
                              }
                            ]
                          },
                          {
                            "campaignId": "C-00099211",
                            "subCampaignId": null,
                            "attendees": [
                              {
                                "cn": "AURORA99@WEB.COM",
                                "firstName": "Aurora",
                                "lastName": "Conti",
                                "birthDate": "1999-07-14",
                                "partnerId": "2003001",
                                "isCompanion": false,
                                "qrCode": "QR-2"
                              }
                            ]
                          }
                        ]
            """;

    // Parametrized helper method that builds a CRM request.
    private MockHttpServletRequestBuilder genericRequest(String apiKey, String payload) {

        var builder = post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload);

        if (apiKey != null) {
            builder.header(API_KEY_HEADER, apiKey);
        }

        return builder;
    }

    // ---------- SUCCESS CASE ----------
    @Test
    void shouldAcceptRequestAndReturn202() throws Exception {
        mockMvc.perform(genericRequest(API_KEY, VALID_PAYLOAD))
                .andDo(print())
                .andExpect(status().isAccepted());
    }

    // ---------- SECURITY TEST ----------
    @Test
    void shouldReturn401WhenApiKeyMissing() throws Exception {
        mockMvc.perform(genericRequest(null, VALID_PAYLOAD))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ---------- VALIDATION TEST ----------
    @Test
    void shouldReturn400WhenPayloadMalformed() throws Exception {
        mockMvc.perform(genericRequest(API_KEY, INVALID_PAYLOAD))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ---------- MESSAGE PUBLISHING TEST ----------
    @Test
    void shouldPublishMessageToExchange() throws Exception {

        // STEP 1: Perform the Success case
        mockMvc.perform(genericRequest(API_KEY, VALID_PAYLOAD))
                .andDo(print())
                .andExpect(status().isAccepted());

        // STEP 2: Retrieve the metadata/properties of the specific queue from the RabbitAdmin
        Properties queueProperties = rabbitAdmin.getQueueProperties(QUEUE_NAME);
        assert queueProperties != null;

        // STEP 3: Extract the message count. RabbitAdmin returns raw Properties, so they can be converted from String to Integer
        Object raw = queueProperties.get(RabbitAdmin.QUEUE_MESSAGE_COUNT);
        int messageCount = Integer.parseInt(raw.toString());

        // STEP 4: Assert that the queue exists and contains exactly 2 messages as expected from the payload
        assertThat(queueProperties).isNotNull();
        assertThat(messageCount).isEqualTo(2);

        // STEP 5: Pull messages from the queue for inspection; timeout is for managing a potential asynchronous processing delays
        Message firstMessage = rabbitTemplate.receive(QUEUE_NAME, 15000);
        Message secondMessage = rabbitTemplate.receive(QUEUE_NAME, 15000);

        // STEP 6: Ensure that both messages were successfully captured from the broker
        assertThat(firstMessage).isNotNull();
        assertThat(secondMessage).isNotNull();

        // STEP 7: Deserialization: Convert JSON message bodies back into Java Objects (DTOs)
        CrmIncomingCampaignDTO firstCampaign = objectMapper.readValue(firstMessage.getBody(), CrmIncomingCampaignDTO.class);
        CrmIncomingCampaignDTO secondCampaign =  objectMapper.readValue(secondMessage.getBody(), CrmIncomingCampaignDTO.class);

        /* Assert: Verify that the campaign IDs match the expected values 'containsExactlyInAnyOrder' is used because message
        delivery order isn't always strictly guaranteed */
        assertThat(List.of(firstCampaign.getCampaignId(), secondCampaign.getCampaignId())).containsExactlyInAnyOrder("C-00088102", "C-00099211");

        // Stop container (custom lifecycle)
        stopContainer();
    }
}






