package com.disasterconnect.controller;

import com.disasterconnect.dto.AlertCreateRequest;
import com.disasterconnect.dto.AlertResponse;
import com.disasterconnect.dto.AlertStatusUpdateRequest;
import com.disasterconnect.dto.ApiResponse;
import com.disasterconnect.security.UserPrincipal;
import com.disasterconnect.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * GET /api/alerts — public endpoint.
     * Supports optional query params: status, affectedArea, page, size.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AlertResponse>>> getAlerts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String affectedArea,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AlertResponse> alerts = alertService.getAlerts(status, affectedArea, page, size);
        return ResponseEntity.ok(ApiResponse.success("Alerts retrieved successfully", alerts));
    }

    /**
     * GET /api/alerts/{id} — public endpoint.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlertResponse>> getAlertById(@PathVariable Long id) {
        AlertResponse alert = alertService.getAlertById(id);
        return ResponseEntity.ok(ApiResponse.success("Alert retrieved successfully", alert));
    }

    /**
     * POST /api/alerts — AUTHORITY or ADMIN only (enforced in SecurityConfig).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AlertResponse>> createAlert(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AlertCreateRequest request) {

        AlertResponse created = alertService.createAlert(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Alert created successfully", created));
    }

    /**
     * PATCH /api/alerts/{id}/status — AUTHORITY or ADMIN only (enforced in SecurityConfig).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AlertResponse>> updateAlertStatus(
            @PathVariable Long id,
            @Valid @RequestBody AlertStatusUpdateRequest request) {

        AlertResponse updated = alertService.updateAlertStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Alert status updated successfully", updated));
    }
}
