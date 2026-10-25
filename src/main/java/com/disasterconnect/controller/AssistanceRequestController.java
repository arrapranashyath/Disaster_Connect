package com.disasterconnect.controller;

import com.disasterconnect.dto.*;
import com.disasterconnect.entity.enums.AssistanceType;
import com.disasterconnect.entity.enums.RequestPriority;
import com.disasterconnect.entity.enums.RequestStatus;
import com.disasterconnect.security.UserPrincipal;
import com.disasterconnect.service.AssistanceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/assistance-requests", "/api/help-requests"})
public class AssistanceRequestController {

    private final AssistanceService assistanceService;

    public AssistanceRequestController(AssistanceService assistanceService) {
        this.assistanceService = assistanceService;
    }

    /**
     * POST /api/assistance-requests — Citizen creates an emergency help request.
     * The citizen identity is derived from the JWT; never from the request body.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AssistanceResponse>> createRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AssistanceCreateRequest request) {

        AssistanceResponse created = assistanceService.createRequest(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Emergency assistance request created successfully", created));
    }

    /**
     * GET /api/assistance-requests — List requests with optional filters.
     * Citizens receive only their own requests; verified responders/coordinators see matching requests.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AssistanceResponse>>> getRequests(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) AssistanceType type,
            @RequestParam(required = false) RequestPriority priority,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AssistanceResponse> results = assistanceService.getRequests(
                principal, status, type, priority, location, page, size);
        return ResponseEntity.ok(ApiResponse.success("Assistance requests retrieved successfully", results));
    }

    /**
     * GET /api/assistance-requests/my — Current citizen's requests.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<AssistanceResponse>>> getMyRequests(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AssistanceResponse> results = assistanceService.getMyRequests(principal, page, size);
        return ResponseEntity.ok(ApiResponse.success("Your assistance requests retrieved successfully", results));
    }

    /**
     * GET /api/assistance-requests/assigned-to-me — Requests assigned to current responder.
     */
    @GetMapping("/assigned-to-me")
    public ResponseEntity<ApiResponse<Page<AssistanceResponse>>> getAssignedToMe(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AssistanceResponse> results = assistanceService.getAssignedToMe(principal, page, size);
        return ResponseEntity.ok(ApiResponse.success("Assigned requests retrieved successfully", results));
    }

    /**
     * GET /api/assistance-requests/{id} — Request details with status transition audit trail.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssistanceResponse>> getRequestById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        AssistanceResponse response = assistanceService.getRequestById(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Assistance request retrieved successfully", response));
    }

    /**
     * POST /api/assistance-requests/{id}/accept — Verified Volunteer or NGO accepts open request.
     */
    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<AssistanceResponse>> acceptRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        AssistanceResponse accepted = assistanceService.acceptRequest(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Assistance request accepted successfully", accepted));
    }

    /**
     * POST /api/assistance-requests/{id}/assign — Verified NGO or Admin assigns a volunteer.
     */
    @PostMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<AssistanceResponse>> assignVolunteer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AssignVolunteerRequest assignReq) {

        AssistanceResponse assigned = assistanceService.assignVolunteer(id, principal, assignReq);
        return ResponseEntity.ok(ApiResponse.success("Volunteer assigned to request successfully", assigned));
    }

    /**
     * PATCH /api/assistance-requests/{id}/status — Update status (responder, NGO, admin).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AssistanceResponse>> updateStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AssistanceStatusUpdateRequest updateReq) {

        AssistanceResponse updated = assistanceService.updateRequestStatus(id, principal, updateReq);
        return ResponseEntity.ok(ApiResponse.success("Request status updated successfully", updated));
    }

    /**
     * PATCH /api/assistance-requests/{id}/cancel — Citizen cancels their own request.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AssistanceResponse>> cancelRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String reason) {

        AssistanceResponse cancelled = assistanceService.cancelRequest(id, principal, reason);
        return ResponseEntity.ok(ApiResponse.success("Assistance request cancelled successfully", cancelled));
    }
}
