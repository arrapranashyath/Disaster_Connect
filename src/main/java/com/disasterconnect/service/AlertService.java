package com.disasterconnect.service;

import com.disasterconnect.dto.AlertCreateRequest;
import com.disasterconnect.dto.AlertResponse;
import com.disasterconnect.dto.AlertStatusUpdateRequest;
import com.disasterconnect.entity.DisasterAlert;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.enums.AlertStatus;
import com.disasterconnect.exception.BadRequestException;
import com.disasterconnect.exception.ResourceNotFoundException;
import com.disasterconnect.repository.DisasterAlertRepository;
import com.disasterconnect.repository.UserRepository;
import com.disasterconnect.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertService {

    private final DisasterAlertRepository alertRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public AlertService(DisasterAlertRepository alertRepository,
                        UserRepository userRepository,
                        NotificationService notificationService) {
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * Create a new disaster alert. The issuing user is derived from the
     * authenticated principal — never from a client-supplied field.
     */
    @Transactional
    public AlertResponse createAlert(UserPrincipal principal, AlertCreateRequest request) {
        User issuedBy = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        DisasterAlert alert = new DisasterAlert(
                request.getDisasterType(),
                request.getTitle(),
                request.getDescription(),
                request.getAffectedArea(),
                request.getSeverity(),
                request.getSafetyInstructions(),
                AlertStatus.ACTIVE,
                issuedBy
        );

        DisasterAlert saved = alertRepository.save(alert);

        // REQ-NOTIF-1: Notify users in affected area
        notificationService.notifyAlertUsers(
                saved.getAffectedArea(),
                "Disaster Alert: " + saved.getTitle(),
                saved.getDescription(),
                saved.getAlertId()
        );

        return toResponse(saved);
    }

    /**
     * List alerts with optional filters. Public endpoint — no auth required.
     *
     * @param status      optional status filter (e.g. "ACTIVE"); if null, no status filter applied
     * @param affectedArea optional case-insensitive substring to search in affected_area
     * @param page        zero-based page index
     * @param size        page size (capped at 100)
     */
    @Transactional(readOnly = true)
    public Page<AlertResponse> getAlerts(String status, String affectedArea, int page, int size) {
        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "issuedAt"));

        Page<DisasterAlert> alerts;

        boolean hasStatus = status != null && !status.isBlank();
        boolean hasArea = affectedArea != null && !affectedArea.isBlank();

        if (hasStatus && hasArea) {
            AlertStatus alertStatus = parseAlertStatus(status);
            alerts = alertRepository.findAllByAffectedAreaContainingIgnoreCaseAndStatus(affectedArea, alertStatus, pageable);
        } else if (hasStatus) {
            AlertStatus alertStatus = parseAlertStatus(status);
            alerts = alertRepository.findAllByStatus(alertStatus, pageable);
        } else if (hasArea) {
            alerts = alertRepository.findAllByAffectedAreaContainingIgnoreCase(affectedArea, pageable);
        } else {
            alerts = alertRepository.findAllByOrderByIssuedAtDesc(pageable);
        }

        return alerts.map(this::toResponse);
    }

    private AlertStatus parseAlertStatus(String status) {
        try {
            return AlertStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid alert status: '" + status + "'. Valid values are: ACTIVE, RESOLVED");
        }
    }

    /**
     * Retrieve a single alert by ID. Public endpoint.
     */
    @Transactional(readOnly = true)
    public AlertResponse getAlertById(Long alertId) {
        DisasterAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));
        return toResponse(alert);
    }

    /**
     * Update the status of an alert (ACTIVE ↔ RESOLVED).
     * Only AUTHORITY and ADMIN may call this — enforced in SecurityConfig.
     */
    @Transactional
    public AlertResponse updateAlertStatus(Long alertId, AlertStatusUpdateRequest request) {
        DisasterAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));
        alert.setStatus(request.getStatus());
        DisasterAlert updated = alertRepository.save(alert);
        return toResponse(updated);
    }

    // ---------------------------------------------------------------------------
    // Mapping helper
    // ---------------------------------------------------------------------------

    private AlertResponse toResponse(DisasterAlert alert) {
        AlertResponse response = new AlertResponse();
        response.setAlertId(alert.getAlertId());
        response.setDisasterType(alert.getDisasterType());
        response.setTitle(alert.getTitle());
        response.setDescription(alert.getDescription());
        response.setAffectedArea(alert.getAffectedArea());
        response.setSeverity(alert.getSeverity());
        response.setSafetyInstructions(alert.getSafetyInstructions());
        response.setStatus(alert.getStatus());
        response.setIssuedAt(alert.getIssuedAt());
        response.setUpdatedAt(alert.getUpdatedAt());

        User issuer = alert.getIssuedBy();
        response.setIssuedBy(new AlertResponse.IssuedByInfo(
                issuer.getUserId(),
                issuer.getName(),
                issuer.getRole().name()
        ));

        return response;
    }
}
