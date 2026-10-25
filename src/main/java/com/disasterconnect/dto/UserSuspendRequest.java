package com.disasterconnect.dto;

import jakarta.validation.constraints.NotNull;

public class UserSuspendRequest {

    @NotNull(message = "Suspended status is required")
    private Boolean suspended;

    private String reason;

    public UserSuspendRequest() {
    }

    public UserSuspendRequest(Boolean suspended, String reason) {
        this.suspended = suspended;
        this.reason = reason;
    }

    public Boolean getSuspended() {
        return suspended;
    }

    public void setSuspended(Boolean suspended) {
        this.suspended = suspended;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
