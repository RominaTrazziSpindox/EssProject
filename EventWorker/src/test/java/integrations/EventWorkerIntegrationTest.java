package integrations;

import com.spx.EventWorkerApplication;
import com.spx.config.RabbitConfig;
import com.spx.dto.CampaignEventDTO;
import com.spx.models.Campaign;
import com.spx.repos.CampaignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = EventWorkerApplication.class)
@Testcontainers
@Import(RabbitConfig.class)
class EventWorkerIntegrationTest extends AbstractIntegrationTest {


    // Constants
    private static final String EXCHANGE = "crm.exchange";
    private static final String ROUTING_KEY = "crm.campaign.created";
    private static final String QUEUE_NAME_DLQ = "crm.campaign.dlq";

    // Configurations
    @BeforeEach
    void setupRabbitTopology() {

        Queue queue = new Queue(QUEUE_NAME_DLQ);
        DirectExchange exchange = new DirectExchange("crm.exchange");

        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareQueue(queue);

        rabbitAdmin.declareBinding(BindingBuilder.bind(queue)
                .to(exchange)
                .with("crm.campaign.created")
        );
    }

    @BeforeEach
    void cleanQueue() {
        rabbitAdmin.purgeQueue(QUEUE_NAME_DLQ, true);
    }

    // Injections
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private CampaignRepository campaignRepository;


    // TEST AREA
    @Test
    void shouldConsumeMessageAndPersistCampaign() {

        CampaignEventDTO campaignEventDTO = TestDataFactory.builderValidCampaignDTO("C-1", "SC-1", 1);

        publish(campaignEventDTO);

        await().atMost(5, SECONDS).untilAsserted(() -> {

            Optional<Campaign> campaignOptional = campaignRepository.findByCampaignIdAndSubCampaignId(
                    campaignEventDTO.getCampaignId(),
                    campaignEventDTO.getSubCampaignId()
            );

            assertTrue(campaignOptional.isPresent(), "Campaign should be present");
            assertEquals(1, getAttendeesCount(campaignEventDTO),"Campaign should have 1 attendee");
        });
    }

    @Test
    void shouldReplaceAttendeesOnUpdate() {

        // STEP 1 - publish first message campaign (2 attendees)
        CampaignEventDTO firstCampaignEventDTO = TestDataFactory.builderValidCampaignDTO("C-1", "SC-1", 2);

        publish(firstCampaignEventDTO);

        await().atMost(5, SECONDS).untilAsserted(() -> {

            long count = getAttendeesCount(firstCampaignEventDTO);

            assertEquals(2, count,"Expected 2 attendees after first message campaign. Attendees found: " + count);

        });

        // STEP 2 - publish second message campaign (1 attendee, same campaign)
        CampaignEventDTO secondCampaignEventDTO = TestDataFactory.builderValidCampaignDTO("C-1", "SC-1", 1);

        publish(secondCampaignEventDTO);

        await().atMost(5, SECONDS).untilAsserted(() -> {

            long count = getAttendeesCount(secondCampaignEventDTO);

            assertEquals(1, count,"The campaign should have 1 attendee after update. Attendees found: " + count);

        });
    }

    @Test
    void shouldBeIdempotent_whenSameMessageIsProcessedTwice() {

        CampaignEventDTO validCampaignDTO = TestDataFactory.builderValidCampaignDTO("C-1", "SC-1", 2);

        // First processing
        publish(validCampaignDTO);

        await().atMost(5, SECONDS).untilAsserted(() ->
                assertEquals(2, getAttendeesCount(validCampaignDTO))
        );

        // Second processing (same message again)
        publish(validCampaignDTO);

        await().atMost(5, SECONDS).untilAsserted(() ->
                assertEquals(2, getAttendeesCount(validCampaignDTO)) // MUST stay 2
        );
    }

    @Test
    void shouldSendMessageToDLQOnFailure() {

        CampaignEventDTO invalidCampaignDTO = TestDataFactory.builderInvalidCampaignDTO();

        publish(invalidCampaignDTO);

        await().atMost(5, SECONDS).untilAsserted(() -> {

            Object message = rabbitTemplate.receiveAndConvert(QUEUE_NAME_DLQ);

            assertNotNull(message, "Message should be in DLQ");
        });
    }

    @Test
    void shouldRouteFailedMessageToDLQ_withCorrectPayload() {

        CampaignEventDTO invalidCampaignDTO = TestDataFactory.builderInvalidCampaignDTO();

        publish(invalidCampaignDTO);

        await().atMost(5, SECONDS).untilAsserted(() -> {

            Object raw = rabbitTemplate.receiveAndConvert(QUEUE_NAME_DLQ);

            assertNotNull(raw, "DLQ message should not be null");

            assertTrue(raw instanceof CampaignEventDTO, "Message should be CampaignEventDTO");

            CampaignEventDTO dlqMessage = (CampaignEventDTO) raw;

            assertEquals(invalidCampaignDTO.getCampaignId(), dlqMessage.getCampaignId());
            assertEquals(invalidCampaignDTO.getSubCampaignId(), dlqMessage.getSubCampaignId());
        });
    }


    // HELPER METHODS
    private void publish(CampaignEventDTO dto) {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, dto);
    }

    private long getAttendeesCount(CampaignEventDTO dto) {
        return campaignRepository.countAttendees(
                dto.getCampaignId(),
                dto.getSubCampaignId()
        );
    }
}