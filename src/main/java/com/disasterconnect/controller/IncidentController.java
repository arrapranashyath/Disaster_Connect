package com.disasterconnect.controller;

import com.disasterconnect.dto.ApiResponse;
import com.disasterconnect.dto.IncidentCreateRequest;
import com.disasterconnect.dto.IncidentResponse;
import com.disasterconnect.dto.IncidentStatusUpdateRequest;
import com.disasterconnect.security.UserPrincipal;
import com.disasterconnect.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    /**
     * POST /api/incidents — CITIZEN (and ADMIN) only (enforced in SecurityConfig).
     * The reporter is derived from the JWT principal; never from the request body.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<IncidentResponse>> submitIncident(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody IncidentCreateRequest request) {

        IncidentResponse created = incidentService.submitIncident(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Incident reported successfully", created));
    }

    /**
     * GET /api/incidents/my — any authenticated user; returns only the caller's own incidents.
     * Must be declared before /{id} to avoid path-variable ambiguity.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<IncidentResponse>>> getMyIncidents(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<IncidentResponse> incidents = incidentService.getMyIncidents(principal, page, size);
        return ResponseEntity.ok(ApiResponse.success("Your incidents retrieved successfully", incidents));
    }

    /**
     * GET /api/incidents — AUTHORITY, ADMIN, NGO only (enforced in SecurityConfig).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<IncidentResponse>>> getAllIncidents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<IncidentResponse> incidents = incidentService.getAllIncidents(page, size);
        return ResponseEntity.ok(ApiResponse.success("Incidents retrieved successfully", incidents));
    }

    /**
     * GET /api/incidents/{id} — authenticated users.
     * Citizens may only retrieve their own incidents (enforced in IncidentService).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentResponse>> getIncidentById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        IncidentResponse incident = incidentService.getIncidentById(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Incident retrieved successfully", incident));
    }

    /**
     * PATCH /api/incidents/{id}/status — AUTHORITY or ADMIN only (enforced in SecurityConfig).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<IncidentResponse>> updateIncidentStatus(
            @PathVariable Long id,
            @Valid @RequestBody IncidentStatusUpdateRequest request) {

        IncidentResponse updated = incidentService.updateIncidentStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Incident status updated successfully", updated));
    }
}
