package com.disasterconnect.dto;

import com.disasterconnect.entity.MissingPerson;
import com.disasterconnect.entity.enums.MissingPersonStatus;

import java.time.Instant;

public class MissingPersonResponse {

    private Long missingId;
    private Long reportedByUserId;
    private String reportedByUserName;
    private String name;
    private Integer age;
    private String gender;
    private String lastSeenLocation;
    private String description;
    private String contactInfo;
    private String photoUrl;
    private MissingPersonStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public MissingPersonResponse() {
    }

    public MissingPersonResponse(Long missingId, Long reportedByUserId, String reportedByUserName,
                                 String name, Integer age, String gender, String lastSeenLocation,
                                 String description, String contactInfo, String photoUrl,
                                 MissingPersonStatus status, Instant createdAt, Instant updatedAt) {
        this.missingId = missingId;
        this.reportedByUserId = reportedByUserId;
        this.reportedByUserName = reportedByUserName;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.lastSeenLocation = lastSeenLocation;
        this.description = description;
        this.contactInfo = contactInfo;
        this.photoUrl = photoUrl;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MissingPersonResponse fromEntity(MissingPerson mp, boolean maskContactInfo) {
        if (mp == null) {
            return null;
        }

        String contact = maskContactInfo ? "[PROTECTED - LOGIN TO VIEW]" : mp.getContactInfo();
        Long reporterId = mp.getReportedBy() != null ? mp.getReportedBy().getUserId() : null;
        String reporterName = mp.getReportedBy() != null ? mp.getReportedBy().getName() : null;

        return new MissingPersonResponse(
                mp.getMissingId(),
                reporterId,
                reporterName,
                mp.getName(),
                mp.getAge(),
                mp.getGender(),
                mp.getLastSeenLocation(),
                mp.getDescription(),
                contact,
                mp.getPhotoUrl(),
                mp.getStatus(),
                mp.getCreatedAt(),
                mp.getUpdatedAt()
        );
    }

    public Long getMissingId() {
        return missingId;
    }

    public void setMissingId(Long missingId) {
        this.missingId = missingId;
    }

    public Long getReportedByUserId() {
        return reportedByUserId;
    }

    public void setReportedByUserId(Long reportedByUserId) {
        this.reportedByUserId = reportedByUserId;
    }

    public String getReportedByUserName() {
        return reportedByUserName;
    }

    public void setReportedByUserName(String reportedByUserName) {
        this.reportedByUserName = reportedByUserName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLastSeenLocation() {
        return lastSeenLocation;
    }

    public void setLastSeenLocation(String lastSeenLocation) {
        this.lastSeenLocation = lastSeenLocation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public MissingPersonStatus getStatus() {
        return status;
    }

    public void setStatus(MissingPersonStatus status) {
        this.status = status;
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
