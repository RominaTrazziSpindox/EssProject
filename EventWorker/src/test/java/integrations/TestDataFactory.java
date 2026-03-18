package integrations;
import com.spx.dto.AttendeeDTO;
import com.spx.dto.CampaignEventDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

public class TestDataFactory {

    public static CampaignEventDTO campaignWithSingleAttendee() {
        return CampaignEventDTO.builder()
                .campaignId("C-TEST-SINGLE-ATTENDEE")
                .subCampaignId("SC-1")
                .attendees(List.of(attendee("Matteo")))
                .build();
    }

    public static CampaignEventDTO campaignWithMultipleAttendees(int count) {
        return CampaignEventDTO.builder()
                .campaignId("C-TEST-MULTIPLE-ATTENDEES")
                .subCampaignId("SC-2")
                .attendees(
                        IntStream.range(0, count)
                                .mapToObj(i -> attendee("Name" + i))
                                .toList()
                )
                .build();
    }

    public static CampaignEventDTO campaignWithSingleAttendeeAndSameId() {
        return CampaignEventDTO.builder()
                .campaignId("C-TEST-SINGLE-ATTENDEE-SAME-ID")
                .subCampaignId("SC-2")
                .attendees(List.of(attendee("OnlyOne")))
                .build();
    }

    public static CampaignEventDTO invalidCampaign() {
        return CampaignEventDTO.builder()
                .campaignId("C-ERR-CAMPAIGN")
                .subCampaignId("SC-ERR-CAMPAIGN")
                .attendees(List.of(
                        AttendeeDTO.builder()
                                .firstName(null) // caused error
                                .lastName("Error")
                                .partnerId("1")
                                .isCompanion(false)
                                .qrCode("qr")
                                .build()
                ))
                .build();
    }

    private static AttendeeDTO attendee(String name) {
        return AttendeeDTO.builder()
                .firstName(name)
                .lastName("Test")
                .birthDate(LocalDate.of(1990, 1, 1))
                .partnerId("1")
                .cn("cn")
                .isCompanion(false)
                .qrCode("qr-" + name)
                .build();
    }

}