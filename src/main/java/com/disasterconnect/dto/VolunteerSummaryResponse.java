package com.disasterconnect.dto;

import com.disasterconnect.entity.enums.VerificationStatus;

public class VolunteerSummaryResponse {

    private Long userId;
    private Long volunteerId;
    private String name;
    private String email;
    private String phone;
    private String skills;
    private String availability;
    private VerificationStatus verificationStatus;
    private long activeTasksCount;

    public VolunteerSummaryResponse() {
    }

    public VolunteerSummaryResponse(Long userId, Long volunteerId, String name, String email,
                                   String phone, String skills, String availability,
                                   VerificationStatus verificationStatus, long activeTasksCount) {
        this.userId = userId;
        this.volunteerId = volunteerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.skills = skills;
        this.availability = availability;
        this.verificationStatus = verificationStatus;
        this.activeTasksCount = activeTasksCount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(Long volunteerId) {
        this.volunteerId = volunteerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public long getActiveTasksCount() {
        return activeTasksCount;
    }

    public void setActiveTasksCount(long activeTasksCount) {
        this.activeTasksCount = activeTasksCount;
    }
}
