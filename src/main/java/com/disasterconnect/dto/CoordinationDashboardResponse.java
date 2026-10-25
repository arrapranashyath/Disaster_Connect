package com.disasterconnect.dto;

import java.util.List;

public class CoordinationDashboardResponse {

    private long totalPendingRequests;
    private long totalAssignedRequests;
    private long totalInProgressRequests;
    private long totalCompletedRequests;
    private long availableVolunteersCount;
    private List<AssistanceResponse> activeRequests;
    private List<VolunteerSummaryResponse> availableVolunteers;

    public CoordinationDashboardResponse() {
    }

    public CoordinationDashboardResponse(long totalPendingRequests, long totalAssignedRequests,
                                         long totalInProgressRequests, long totalCompletedRequests,
                                         long availableVolunteersCount,
                                         List<AssistanceResponse> activeRequests,
                                         List<VolunteerSummaryResponse> availableVolunteers) {
        this.totalPendingRequests = totalPendingRequests;
        this.totalAssignedRequests = totalAssignedRequests;
        this.totalInProgressRequests = totalInProgressRequests;
        this.totalCompletedRequests = totalCompletedRequests;
        this.availableVolunteersCount = availableVolunteersCount;
        this.activeRequests = activeRequests;
        this.availableVolunteers = availableVolunteers;
    }

    public long getTotalPendingRequests() {
        return totalPendingRequests;
    }

    public void setTotalPendingRequests(long totalPendingRequests) {
        this.totalPendingRequests = totalPendingRequests;
    }

    public long getTotalAssignedRequests() {
        return totalAssignedRequests;
    }

    public void setTotalAssignedRequests(long totalAssignedRequests) {
        this.totalAssignedRequests = totalAssignedRequests;
    }

    public long getTotalInProgressRequests() {
        return totalInProgressRequests;
    }

    public void setTotalInProgressRequests(long totalInProgressRequests) {
        this.totalInProgressRequests = totalInProgressRequests;
    }

    public long getTotalCompletedRequests() {
        return totalCompletedRequests;
    }

    public void setTotalCompletedRequests(long totalCompletedRequests) {
        this.totalCompletedRequests = totalCompletedRequests;
    }

    public long getAvailableVolunteersCount() {
        return availableVolunteersCount;
    }

    public void setAvailableVolunteersCount(long availableVolunteersCount) {
        this.availableVolunteersCount = availableVolunteersCount;
    }

    public List<AssistanceResponse> getActiveRequests() {
        return activeRequests;
    }

    public void setActiveRequests(List<AssistanceResponse> activeRequests) {
        this.activeRequests = activeRequests;
    }

    public List<VolunteerSummaryResponse> getAvailableVolunteers() {
        return availableVolunteers;
    }

    public void setAvailableVolunteers(List<VolunteerSummaryResponse> availableVolunteers) {
        this.availableVolunteers = availableVolunteers;
    }
}
