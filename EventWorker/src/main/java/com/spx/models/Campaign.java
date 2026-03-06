package com.spx.models;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "campaigns",uniqueConstraints = { @UniqueConstraint(columnNames = {"campaign_id", "sub_campaign_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campaign_id", nullable = false)
    private String campaignId;

    @Column(name = "sub_campaign_id")
    private String subCampaignId;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)

    @Builder.Default
    private List<Attendee> attendees = new ArrayList<>();

}