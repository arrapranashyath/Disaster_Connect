package com.disasterconnect.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MissingPersonReportRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age cannot be negative")
    @Max(value = 150, message = "Age must be valid")
    private Integer age;

    @NotBlank(message = "Gender is required")
    @Size(max = 20, message = "Gender must not exceed 20 characters")
    private String gender;

    @NotBlank(message = "Last seen location is required")
    @Size(max = 255, message = "Last seen location must not exceed 255 characters")
    private String lastSeenLocation;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Contact info is required")
    @Size(max = 255, message = "Contact info must not exceed 255 characters")
    private String contactInfo;

    @Size(max = 500, message = "Photo URL must not exceed 500 characters")
    private String photoUrl;

    public MissingPersonReportRequest() {
    }

    public MissingPersonReportRequest(String name, Integer age, String gender, String lastSeenLocation,
                                      String description, String contactInfo, String photoUrl) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.lastSeenLocation = lastSeenLocation;
        this.description = description;
        this.contactInfo = contactInfo;
        this.photoUrl = photoUrl;
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
}
