package com.disasterconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class CampaignCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Target amount is required")
    @Positive(message = "Target amount must be positive")
    private Double targetAmount;

    @NotBlank(message = "Donation instructions are required")
    private String donationInstructions;

    private Instant startDate;
    private Instant endDate;

    public CampaignCreateRequest() {
    }

    public CampaignCreateRequest(String title, String description, Double targetAmount,
                                 String donationInstructions, Instant startDate, Instant endDate) {
        this.title = title;
        this.description = description;
        this.targetAmount = targetAmount;
        this.donationInstructions = donationInstructions;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(Double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public String getDonationInstructions() {
        return donationInstructions;
    }

    public void setDonationInstructions(String donationInstructions) {
        this.donationInstructions = donationInstructions;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }
}
