package integrations;

import com.spx.IngestionAPIApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(classes = IngestionAPIApplication.class)
@AutoConfigureMockMvc
class IngestionApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // RabbitMQ container
    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management");


    // Spring Boot connects to the container
    @DynamicPropertySource
    static void configureRabbit(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
    }

    // Valid CRM payload
    String validPayload =
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

    // ---------- SUCCESS CASE ----------

    @Test
    void shouldAcceptRequestAndReturn202() throws Exception {

        mockMvc.perform(post("/api/v1/crm/sync")
            .header("X-API-KEY", "secret-key")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validPayload))
            .andExpect(status().isAccepted());
    }

    // ---------- SECURITY TEST ----------

    @Test
    void shouldReturn401WhenApiKeyMissing() throws Exception {

        mockMvc.perform(post("/api/v1/crm/sync")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validPayload))
            .andExpect(status().isUnauthorized());
    }

    // ---------- VALIDATION TEST ----------

    @Test
    void shouldReturn400WhenPayloadMalformed() throws Exception {

        String invalidPayload = "{ invalid json }";

        mockMvc.perform(post("/api/v1/crm/sync")
            .header("X-API-KEY", "test-key")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidPayload)
        )
        .andExpect(status().isBadRequest());
    }


    // ---------- PUBLISH MESSAGES ON RABBIT MQ ----------

    @Test
    void shouldAcceptRequestAndPublishMessage() throws Exception {

        mockMvc.perform(
            post("/api/v1/crm/sync")
                .header("X-API-KEY", "test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
                .andExpect(status().isAccepted());

        Object message = rabbitTemplate.receiveAndConvert("crm.campaigns.queue");

        assertNotNull(message);
    }

}