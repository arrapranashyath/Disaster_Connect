package com.disasterconnect.dto;

import com.disasterconnect.entity.enums.IncidentStatus;
import jakarta.validation.constraints.NotNull;

public class IncidentStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private IncidentStatus status;

    public IncidentStatusUpdateRequest() {
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }
}
