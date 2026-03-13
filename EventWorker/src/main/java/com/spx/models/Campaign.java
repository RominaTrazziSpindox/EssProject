package com.spx.models;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "campaigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campaign_id", nullable = false, unique = true)
    private String campaignId;

    @Column(name = "sub_campaign_id")
    private String subCampaignId;


    /* When a campaign is deleted, all related attendees are removed due to cascade and orphanRemoval
    MappedBy is linked to the attribute "campaign" in Attendee table */
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Attendee> attendees = new ArrayList<>();

    // Method
    public void addAttendee(Attendee attendee) {
        attendees.add(attendee);
        attendee.setCampaign(this);
    }
}