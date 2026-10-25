package com.disasterconnect.dto;

import com.disasterconnect.entity.enums.AlertStatus;
import jakarta.validation.constraints.NotNull;

public class AlertStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private AlertStatus status;

    public AlertStatusUpdateRequest() {
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }
}
