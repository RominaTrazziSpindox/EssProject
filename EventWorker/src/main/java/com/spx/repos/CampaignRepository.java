package com.spx.repos;

import com.spx.models.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Optional<Campaign> findByCampaignIdAndSubCampaignId(
            String campaignId,
            String subCampaignId
    );

    @Query("""
        SELECT COUNT(a) FROM Attendee a
        WHERE a.campaign.campaignId = :campaignId
        AND a.campaign.subCampaignId = :subCampaignId
    """)
    long countAttendees(String campaignId, String subCampaignId);

}