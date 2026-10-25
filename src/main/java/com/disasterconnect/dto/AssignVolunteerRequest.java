package com.disasterconnect.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AssignVolunteerRequest {

    @NotNull(message = "Volunteer user ID is required")
    private Long volunteerUserId;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;

    public AssignVolunteerRequest() {
    }

    public AssignVolunteerRequest(Long volunteerUserId, String notes) {
        this.volunteerUserId = volunteerUserId;
        this.notes = notes;
    }

    public Long getVolunteerUserId() {
        return volunteerUserId;
    }

    public void setVolunteerUserId(Long volunteerUserId) {
        this.volunteerUserId = volunteerUserId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
