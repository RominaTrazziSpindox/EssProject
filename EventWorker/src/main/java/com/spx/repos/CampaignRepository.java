package com.spx.repos;

import com.spx.models.Campaign;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    /**
     * Finds a campaign by its business identifiers.
     * This method is used to locate an existing campaign during
     * synchronization, in order to decide whether the incoming payload
     * should create a new record or update an existing one.
     *
     * @param campaignId the main campaign identifier
     * @param subCampaignId the optional sub-campaign identifier
     * @return the matching campaign, if present
     */
    Optional<Campaign> findByCampaignIdAndSubCampaignId(
            String campaignId,
            String subCampaignId
    );


    /**
     * Counts the number of attendees associated with the given campaign.
     *
     * @param campaignId the main campaign identifier
     * @param subCampaignId the optional sub-campaign identifier
     * @return the number of attendees linked to the selected campaign
     */
    @Query("""
        SELECT COUNT(a) FROM Attendee a
        WHERE a.campaign.campaignId = :campaignId
        AND a.campaign.subCampaignId = :subCampaignId
    """)
    long countAttendees(String campaignId, String subCampaignId);


    /**
     * Retrieves all campaigns with attendees already loaded.
     * This method is used by the reporting flow to avoid lazy loading issues.
     *
     * @return all campaigns ordered by campaign identifier
     */
    @EntityGraph(attributePaths = "attendees")
    List<Campaign> findAllByOrderByCampaignIdAsc();
}

