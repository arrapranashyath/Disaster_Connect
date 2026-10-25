package com.disasterconnect.repository;

import com.disasterconnect.entity.NGOProfile;
import com.disasterconnect.entity.ReliefCampaign;
import com.disasterconnect.entity.enums.CampaignStatus;
import com.disasterconnect.entity.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReliefCampaignRepository extends JpaRepository<ReliefCampaign, Long> {

    List<ReliefCampaign> findByStatusAndVerificationStatus(CampaignStatus status, VerificationStatus verificationStatus);

    Page<ReliefCampaign> findByStatusAndVerificationStatus(CampaignStatus status, VerificationStatus verificationStatus, Pageable pageable);

    List<ReliefCampaign> findByOrganization(NGOProfile organization);

    Page<ReliefCampaign> findByOrganization(NGOProfile organization, Pageable pageable);

    Page<ReliefCampaign> findByVerificationStatus(VerificationStatus verificationStatus, Pageable pageable);
}
