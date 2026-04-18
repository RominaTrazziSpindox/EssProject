package integrations;

import com.spx.EventWorkerApplication;
import com.spx.config.RabbitProperties;
import com.spx.dto.CampaignEventDTO;
import com.spx.models.Campaign;
import com.spx.repos.CampaignRepository;
import com.spx.services.CampaignProcessService;
import org.junit.jupiter.api.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.Properties;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(classes = EventWorkerApplication.class)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EventWorkerIntegrationTest extends AbstractIntegrationTest {

    // Configurations
    @BeforeEach
    void cleanState() {
        rabbitAdmin.purgeQueue(rabbitProperties.getQueue(), true);
        rabbitAdmin.purgeQueue(rabbitProperties.getDlq(), true);
        campaignRepository.deleteAll();
    }

    // Injections
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private RabbitProperties rabbitProperties;

    @MockitoSpyBean
    private CampaignProcessService campaignProcessService;


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
            assertEquals(1, getAttendeesCount(campaignEventDTO), "Campaign should have 1 attendee");
        });
    }

    @Test
    void shouldReplaceAttendeesOnUpdate() {

        // STEP 1 - publish first message campaign (2 attendees)
        CampaignEventDTO firstCampaignEventDTO = TestDataFactory.builderValidCampaignDTO("C-1", "SC-1", 2);

        publish(firstCampaignEventDTO);

        await().atMost(5, SECONDS).untilAsserted(() -> {

            long count = getAttendeesCount(firstCampaignEventDTO);

            assertEquals(2, count, "Expected 2 attendees after first message campaign. Attendees found: " + count);
        });

        // STEP 2 - publish second message campaign (1 attendee, same campaign)
        CampaignEventDTO secondCampaignEventDTO = TestDataFactory.builderValidCampaignDTO("C-1", "SC-1", 1);

        publish(secondCampaignEventDTO);

        await().atMost(5, SECONDS).untilAsserted(() -> {

            long count = getAttendeesCount(secondCampaignEventDTO);

            assertEquals(1, count, "The campaign should have 1 attendee after update. Attendees found: " + count);
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

        CampaignEventDTO DLQCampaignDTO = TestDataFactory.builderDLQCampaignDTO();

        doThrow(new RuntimeException("Forced consumer failure"))
                .when(campaignProcessService)
                .processCampaignFromRabbit(argThat(dto ->
                        "C-DLQ".equals(dto.getCampaignId()) && "SC-DLQ".equals(dto.getSubCampaignId())
                ));

        publish(DLQCampaignDTO);

        await().atMost(5, SECONDS).untilAsserted(() -> {

            Properties queueProperties = rabbitAdmin.getQueueProperties(rabbitProperties.getDlq());

            assertNotNull(queueProperties, "DLQ should exist");

            Object raw = queueProperties.get(RabbitAdmin.QUEUE_MESSAGE_COUNT);
            int messageCount = Integer.parseInt(raw.toString());

            assertEquals(1, messageCount, "DLQ should contain 1 message");
        });

        Object message = rabbitTemplate.receiveAndConvert(rabbitProperties.getDlq());

        assertNotNull(message, "Message should be in DLQ");
    }

    // HELPER METHODS
    private void publish(CampaignEventDTO dto) {
        rabbitTemplate.convertAndSend(
                rabbitProperties.getExchange(),
                rabbitProperties.getRoutingKey(),
                dto
        );
    }

    private long getAttendeesCount(CampaignEventDTO dto) {
        return campaignRepository.countAttendees(
                dto.getCampaignId(),
                dto.getSubCampaignId()
        );
    }
}