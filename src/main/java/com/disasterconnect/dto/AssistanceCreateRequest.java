package com.disasterconnect.dto;

import com.disasterconnect.entity.enums.AssistanceType;
import com.disasterconnect.entity.enums.RequestPriority;
import jakarta.validation.constraints.*;

public class AssistanceCreateRequest {

    @NotNull(message = "Assistance type is required")
    private AssistanceType assistanceType;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Location name is required")
    @Size(max = 255, message = "Location name must not exceed 255 characters")
    private String locationName;

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;

    @NotNull(message = "Number of people is required")
    @Min(value = 1, message = "Number of people must be at least 1")
    private Integer peopleCount = 1;

    @NotNull(message = "Priority is required")
    private RequestPriority priority = RequestPriority.MEDIUM;

    public AssistanceCreateRequest() {
    }

    public AssistanceType getAssistanceType() {
        return assistanceType;
    }

    public void setAssistanceType(AssistanceType assistanceType) {
        this.assistanceType = assistanceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(Integer peopleCount) {
        this.peopleCount = peopleCount;
    }

    public RequestPriority getPriority() {
        return priority;
    }

    public void setPriority(RequestPriority priority) {
        this.priority = priority;
    }
}
