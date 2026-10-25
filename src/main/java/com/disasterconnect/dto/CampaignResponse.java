package com.disasterconnect.dto;

import com.disasterconnect.entity.ReliefCampaign;
import com.disasterconnect.entity.enums.CampaignStatus;
import com.disasterconnect.entity.enums.VerificationStatus;

import java.time.Instant;

public class CampaignResponse {

    private Long campaignId;
    private Long ngoId;
    private String ngoName;
    private String title;
    private String description;
    private Double targetAmount;
    private Double raisedAmount;
    private String donationInstructions;
    private CampaignStatus status;
    private VerificationStatus verificationStatus;
    private Instant startDate;
    private Instant endDate;
    private Instant createdAt;
    private Instant updatedAt;

    public CampaignResponse() {
    }

    public CampaignResponse(Long campaignId, Long ngoId, String ngoName, String title,
                            String description, Double targetAmount, Double raisedAmount,
                            String donationInstructions, CampaignStatus status,
                            VerificationStatus verificationStatus, Instant startDate,
                            Instant endDate, Instant createdAt, Instant updatedAt) {
        this.campaignId = campaignId;
        this.ngoId = ngoId;
        this.ngoName = ngoName;
        this.title = title;
        this.description = description;
        this.targetAmount = targetAmount;
        this.raisedAmount = raisedAmount;
        this.donationInstructions = donationInstructions;
        this.status = status;
        this.verificationStatus = verificationStatus;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CampaignResponse fromEntity(ReliefCampaign campaign) {
        if (campaign == null) {
            return null;
        }

        Long ngoId = campaign.getOrganization() != null ? campaign.getOrganization().getNgoId() : null;
        String ngoName = campaign.getOrganization() != null ? campaign.getOrganization().getOrgName() : null;

        return new CampaignResponse(
                campaign.getCampaignId(),
                ngoId,
                ngoName,
                campaign.getTitle(),
                campaign.getDescription(),
                campaign.getTargetAmount(),
                campaign.getRaisedAmount(),
                campaign.getDonationInstructions(),
                campaign.getStatus(),
                campaign.getVerificationStatus(),
                campaign.getStartDate(),
                campaign.getEndDate(),
                campaign.getCreatedAt(),
                campaign.getUpdatedAt()
        );
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public Long getNgoId() {
        return ngoId;
    }

    public void setNgoId(Long ngoId) {
        this.ngoId = ngoId;
    }

    public String getNgoName() {
        return ngoName;
    }

    public void setNgoName(String ngoName) {
        this.ngoName = ngoName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(Double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public Double getRaisedAmount() {
        return raisedAmount;
    }

    public void setRaisedAmount(Double raisedAmount) {
        this.raisedAmount = raisedAmount;
    }

    public String getDonationInstructions() {
        return donationInstructions;
    }

    public void setDonationInstructions(String donationInstructions) {
        this.donationInstructions = donationInstructions;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
