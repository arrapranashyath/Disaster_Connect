package com.disasterconnect.dto;

import com.disasterconnect.entity.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AssistanceStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private RequestStatus status;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;

    public AssistanceStatusUpdateRequest() {
    }

    public AssistanceStatusUpdateRequest(RequestStatus status, String notes) {
        this.status = status;
        this.notes = notes;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
