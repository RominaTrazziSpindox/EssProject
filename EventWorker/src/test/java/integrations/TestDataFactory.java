package integrations;

import com.spx.dto.AttendeeDTO;
import com.spx.dto.CampaignEventDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

public class TestDataFactory {

    public static CampaignEventDTO builderValidCampaignDTO(
            String campaignId,
            String subCampaignId,
            int attendeesCount) {

        return CampaignEventDTO.builder()
                .campaignId(campaignId)
                .subCampaignId(subCampaignId)
                .attendees(
                        IntStream.range(0, attendeesCount)
                                .mapToObj(TestDataFactory::attendee)
                                .toList()
                )
                .build();
    }

    public static CampaignEventDTO builderInvalidCampaignDTO() {
        return CampaignEventDTO.builder()
                .campaignId("C-ERR")
                .subCampaignId("SC-ERR")
                .attendees(List.of(
                        AttendeeDTO.builder()
                                .firstName(null) // trigger validation error
                                .lastName("Error")
                                .partnerId("1")
                                .isCompanion(false)
                                .qrCode("qr-error")
                                .build()
                ))
                .build();
    }

    private static AttendeeDTO attendee(int index) {
        return AttendeeDTO.builder()
                .firstName("Name" + index)
                .lastName("Test")
                .birthDate(LocalDate.of(1990, 1, 1))
                .partnerId("P-" + index)
                .cn("cn-" + index)
                .isCompanion(false)
                .qrCode("qr-" + index)
                .build();
    }
}