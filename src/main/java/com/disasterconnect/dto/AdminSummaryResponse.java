package com.disasterconnect.dto;

import java.util.Map;

public class AdminSummaryResponse {

    private long totalUsers;
    private long totalIncidents;
    private long totalAlerts;
    private long totalAssistanceRequests;
    private long pendingAssistanceRequests;
    private long totalNgos;
    private long pendingNgos;
    private long totalVolunteers;
    private long pendingVolunteers;
    private long activeCampaigns;
    private long missingPersonsCount;
    private Map<String, Long> requestsByStatus;

    public AdminSummaryResponse() {
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalIncidents() {
        return totalIncidents;
    }

    public void setTotalIncidents(long totalIncidents) {
        this.totalIncidents = totalIncidents;
    }

    public long getTotalAlerts() {
        return totalAlerts;
    }

    public void setTotalAlerts(long totalAlerts) {
        this.totalAlerts = totalAlerts;
    }

    public long getTotalAssistanceRequests() {
        return totalAssistanceRequests;
    }

    public void setTotalAssistanceRequests(long totalAssistanceRequests) {
        this.totalAssistanceRequests = totalAssistanceRequests;
    }

    public long getPendingAssistanceRequests() {
        return pendingAssistanceRequests;
    }

    public void setPendingAssistanceRequests(long pendingAssistanceRequests) {
        this.pendingAssistanceRequests = pendingAssistanceRequests;
    }

    public long getTotalNgos() {
        return totalNgos;
    }

    public void setTotalNgos(long totalNgos) {
        this.totalNgos = totalNgos;
    }

    public long getPendingNgos() {
        return pendingNgos;
    }

    public void setPendingNgos(long pendingNgos) {
        this.pendingNgos = pendingNgos;
    }

    public long getTotalVolunteers() {
        return totalVolunteers;
    }

    public void setTotalVolunteers(long totalVolunteers) {
        this.totalVolunteers = totalVolunteers;
    }

    public long getPendingVolunteers() {
        return pendingVolunteers;
    }

    public void setPendingVolunteers(long pendingVolunteers) {
        this.pendingVolunteers = pendingVolunteers;
    }

    public long getActiveCampaigns() {
        return activeCampaigns;
    }

    public void setActiveCampaigns(long activeCampaigns) {
        this.activeCampaigns = activeCampaigns;
    }

    public long getMissingPersonsCount() {
        return missingPersonsCount;
    }

    public void setMissingPersonsCount(long missingPersonsCount) {
        this.missingPersonsCount = missingPersonsCount;
    }

    public Map<String, Long> getRequestsByStatus() {
        return requestsByStatus;
    }

    public void setRequestsByStatus(Map<String, Long> requestsByStatus) {
        this.requestsByStatus = requestsByStatus;
    }
}
