package com.spx.repos;

import com.spx.models.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Optional<Campaign> findByCampaignIdAndSubCampaignId(
            String campaignId,
            String subCampaignId
    );

}