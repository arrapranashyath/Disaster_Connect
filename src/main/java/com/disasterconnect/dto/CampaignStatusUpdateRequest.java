package com.disasterconnect.dto;

import com.disasterconnect.entity.enums.CampaignStatus;
import jakarta.validation.constraints.NotNull;

public class CampaignStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private CampaignStatus status;

    public CampaignStatusUpdateRequest() {
    }

    public CampaignStatusUpdateRequest(CampaignStatus status) {
        this.status = status;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }
}
