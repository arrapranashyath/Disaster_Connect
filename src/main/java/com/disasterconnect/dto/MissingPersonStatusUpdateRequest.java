package com.disasterconnect.dto;

import com.disasterconnect.entity.enums.MissingPersonStatus;
import jakarta.validation.constraints.NotNull;

public class MissingPersonStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private MissingPersonStatus status;

    public MissingPersonStatusUpdateRequest() {
    }

    public MissingPersonStatusUpdateRequest(MissingPersonStatus status) {
        this.status = status;
    }

    public MissingPersonStatus getStatus() {
        return status;
    }

    public void setStatus(MissingPersonStatus status) {
        this.status = status;
    }
}
