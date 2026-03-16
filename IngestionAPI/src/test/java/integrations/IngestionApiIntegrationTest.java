package integrations;

import com.spx.IngestionAPIApplication;
import com.spx.config.RabbitConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(classes = IngestionAPIApplication.class)
@AutoConfigureMockMvc
@Import(RabbitConfig.class)
class IngestionApiIntegrationTest {

    /*
     * Testcontainer that starts a real RabbitMQ instance for the duration of the test suite.
     * This allows us to test the application against a real broker instead of mocks.
     */
    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management");

    /*
     * Spring Boot dynamic properties override.
     * The application will connect to the RabbitMQ container started by Testcontainers.
     */
    @DynamicPropertySource
    static void configureRabbit(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
    }

    /*
     * MockitoBean replaces the real RabbitTemplate bean with a Mockito's one.
     * This allows us to verify that the publisher sends messages to RabbitMQ.
     */
    @Autowired
    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    // MockMvc simulates HTTP requests (Get, Post...) without starting a real web server.
    @Autowired
    private MockMvc mockMvc;

    // Minimal valid payload representing a CRM campaign.
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
                    "qrCode": "testqr"
                  }
                ]
              }
            ]
            """;

    // ---- Test constants ----
    private static final String ENDPOINT = "/api/v1/crm/sync";
    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_KEY = "secret-key";
    private static final String INVALID_PAYLOAD = "{ invalid json }";

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
    void shouldPublishMessageOnRabbit() throws Exception {
        mockMvc.perform(genericRequest(API_KEY, VALID_PAYLOAD))
                .andDo(print())
                .andExpect(status().isAccepted());

        verify(rabbitTemplate)
                .convertAndSend(eq("crm.exchange"), eq("crm.campaign.created"), Optional.ofNullable(any()));
    }
}


