package com.disasterconnect.dto;

import com.disasterconnect.entity.NGOProfile;
import com.disasterconnect.entity.enums.VerificationStatus;

import java.time.Instant;

public class NgoVerificationResponse {

    private Long ngoId;
    private Long userId;
    private String orgName;
    private String registrationNumber;
    private VerificationStatus verificationStatus;
    private String email;
    private String phone;
    private String location;
    private Instant createdAt;

    public NgoVerificationResponse() {
    }

    public NgoVerificationResponse(Long ngoId, Long userId, String orgName, String registrationNumber,
                                   VerificationStatus verificationStatus, String email, String phone,
                                   String location, Instant createdAt) {
        this.ngoId = ngoId;
        this.userId = userId;
        this.orgName = orgName;
        this.registrationNumber = registrationNumber;
        this.verificationStatus = verificationStatus;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.createdAt = createdAt;
    }

    public static NgoVerificationResponse fromEntity(NGOProfile ngo) {
        if (ngo == null) {
            return null;
        }

        Long userId = ngo.getUser() != null ? ngo.getUser().getUserId() : null;
        String email = ngo.getUser() != null ? ngo.getUser().getEmail() : null;
        String phone = ngo.getUser() != null ? ngo.getUser().getPhone() : null;
        String location = ngo.getUser() != null ? ngo.getUser().getLocation() : null;
        Instant createdAt = ngo.getCreatedAt();

        return new NgoVerificationResponse(
                ngo.getNgoId(),
                userId,
                ngo.getOrgName(),
                ngo.getRegistrationNumber(),
                ngo.getVerificationStatus(),
                email,
                phone,
                location,
                createdAt
        );
    }

    public Long getNgoId() {
        return ngoId;
    }

    public void setNgoId(Long ngoId) {
        this.ngoId = ngoId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
