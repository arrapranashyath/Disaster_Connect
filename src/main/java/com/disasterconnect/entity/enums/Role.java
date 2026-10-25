package com.disasterconnect.entity.enums;

public enum Role {
    CITIZEN,
    VOLUNTEER,
    NGO,
    AUTHORITY,
    ADMIN;

    public String toAuthority() {
        return "ROLE_" + this.name();
    }
}
