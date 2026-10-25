package com.disasterconnect.service;

import com.disasterconnect.dto.CampaignCreateRequest;
import com.disasterconnect.dto.CampaignResponse;
import com.disasterconnect.dto.CampaignStatusUpdateRequest;
import com.disasterconnect.entity.NGOProfile;
import com.disasterconnect.entity.ReliefCampaign;
import com.disasterconnect.entity.enums.CampaignStatus;
import com.disasterconnect.entity.enums.Role;
import com.disasterconnect.entity.enums.VerificationStatus;
import com.disasterconnect.exception.BadRequestException;
import com.disasterconnect.exception.ResourceNotFoundException;
import com.disasterconnect.repository.NGOProfileRepository;
import com.disasterconnect.repository.ReliefCampaignRepository;
import com.disasterconnect.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CampaignService {

    private final ReliefCampaignRepository campaignRepository;
    private final NGOProfileRepository ngoProfileRepository;

    public CampaignService(ReliefCampaignRepository campaignRepository,
                           NGOProfileRepository ngoProfileRepository) {
        this.campaignRepository = campaignRepository;
        this.ngoProfileRepository = ngoProfileRepository;
    }

    /**
     * REQ-CAMP-1: Only verified NGOs (or Admin) can create relief campaigns.
     */
    @Transactional
    public CampaignResponse createCampaign(UserPrincipal principal, CampaignCreateRequest request) {
        NGOProfile ngoProfile;

        if (principal.getRole() == Role.NGO) {
            ngoProfile = ngoProfileRepository.findByUser_UserId(principal.getUserId())
                    .orElseThrow(() -> new AccessDeniedException("NGO profile not found for current user"));

            if (ngoProfile.getVerificationStatus() != VerificationStatus.VERIFIED) {
                throw new AccessDeniedException("Only verified NGOs can create relief campaigns");
            }
        } else if (principal.getRole() == Role.ADMIN) {
            // If admin creates, link to first available verified NGO or throw if none
            ngoProfile = ngoProfileRepository.findAll().stream()
                    .filter(p -> p.getVerificationStatus() == VerificationStatus.VERIFIED)
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("No verified NGO available to associate with campaign"));
        } else {
            throw new AccessDeniedException("Only NGOs can create relief campaigns");
        }

        Instant start = request.getStartDate() != null ? request.getStartDate() : Instant.now();
        if (request.getEndDate() != null && request.getEndDate().isBefore(start)) {
            throw new BadRequestException("End date cannot be before start date");
        }

        ReliefCampaign campaign = new ReliefCampaign();
        campaign.setOrganization(ngoProfile);
        campaign.setTitle(request.getTitle().trim());
        campaign.setDescription(request.getDescription().trim());
        campaign.setTargetAmount(request.getTargetAmount());
        campaign.setRaisedAmount(0.0);
        campaign.setDonationInstructions(request.getDonationInstructions().trim());
        campaign.setStatus(CampaignStatus.ACTIVE);
        // By default, verified NGO campaigns can be marked VERIFIED or PENDING for admin review
        campaign.setVerificationStatus(VerificationStatus.VERIFIED);
        campaign.setStartDate(start);
        campaign.setEndDate(request.getEndDate());

        ReliefCampaign saved = campaignRepository.save(campaign);
        return CampaignResponse.fromEntity(saved);
    }

    /**
     * REQ-CAMP-2: Public listing of active and verified relief campaigns.
     */
    @Transactional(readOnly = true)
    public Page<CampaignResponse> getActiveVerifiedCampaigns(Pageable pageable) {
        return campaignRepository.findByStatusAndVerificationStatus(
                CampaignStatus.ACTIVE, VerificationStatus.VERIFIED, pageable
        ).map(CampaignResponse::fromEntity);
    }

    /**
     * View single campaign by id.
     */
    @Transactional(readOnly = true)
    public CampaignResponse getCampaignById(Long id) {
        ReliefCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relief campaign not found with id: " + id));
        return CampaignResponse.fromEntity(campaign);
    }

    /**
     * List campaigns by owning NGO.
     */
    @Transactional(readOnly = true)
    public Page<CampaignResponse> getMyCampaigns(UserPrincipal principal, Pageable pageable) {
        NGOProfile ngoProfile = ngoProfileRepository.findByUser_UserId(principal.getUserId())
                .orElseThrow(() -> new AccessDeniedException("NGO profile not found for current user"));
        return campaignRepository.findByOrganization(ngoProfile, pageable)
                .map(CampaignResponse::fromEntity);
    }

    /**
     * REQ-CAMP-3 & REQ-CAMP-4: Update status (e.g. ACTIVE -> COMPLETED, or SUSPENDED).
     * Allowed for owning NGO or Admin.
     */
    @Transactional
    public CampaignResponse updateCampaignStatus(Long id,
                                                CampaignStatusUpdateRequest request,
                                                UserPrincipal principal) {
        ReliefCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relief campaign not found with id: " + id));

        boolean isAdmin = principal.getRole() == Role.ADMIN;
        boolean isOwner = principal.getRole() == Role.NGO &&
                campaign.getOrganization().getUser().getUserId().equals(principal.getUserId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You are not authorized to update this campaign");
        }

        campaign.setStatus(request.getStatus());
        ReliefCampaign updated = campaignRepository.save(campaign);
        return CampaignResponse.fromEntity(updated);
    }

    /**
     * Admin verification moderation.
     */
    @Transactional
    public CampaignResponse verifyCampaign(Long id, VerificationStatus status, UserPrincipal principal) {
        if (principal.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only administrators can verify campaigns");
        }

        ReliefCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relief campaign not found with id: " + id));

        campaign.setVerificationStatus(status);
        ReliefCampaign updated = campaignRepository.save(campaign);
        return CampaignResponse.fromEntity(updated);
    }
}
