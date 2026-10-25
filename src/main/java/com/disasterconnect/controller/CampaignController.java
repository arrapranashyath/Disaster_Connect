package com.disasterconnect.controller;

import com.disasterconnect.dto.ApiResponse;
import com.disasterconnect.dto.CampaignCreateRequest;
import com.disasterconnect.dto.CampaignResponse;
import com.disasterconnect.dto.CampaignStatusUpdateRequest;
import com.disasterconnect.entity.enums.VerificationStatus;
import com.disasterconnect.security.UserPrincipal;
import com.disasterconnect.service.CampaignService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    /**
     * POST /api/campaigns — Verified NGO publishes a relief campaign (REQ-CAMP-1).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CampaignResponse>> createCampaign(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CampaignCreateRequest request) {

        CampaignResponse response = campaignService.createCampaign(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Relief campaign created successfully", response));
    }

    /**
     * GET /api/campaigns — Public listing of active verified relief campaigns (REQ-CAMP-2).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CampaignResponse>>> getActiveCampaigns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CampaignResponse> response = campaignService.getActiveVerifiedCampaigns(pageable);
        return ResponseEntity.ok(ApiResponse.success("Active relief campaigns retrieved successfully", response));
    }

    /**
     * GET /api/campaigns/my — List campaigns owned by the current authenticated NGO.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<CampaignResponse>>> getMyCampaigns(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CampaignResponse> response = campaignService.getMyCampaigns(principal, pageable);
        return ResponseEntity.ok(ApiResponse.success("My relief campaigns retrieved successfully", response));
    }

    /**
     * GET /api/campaigns/{id} — View campaign details including donation instructions.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignResponse>> getCampaignById(@PathVariable Long id) {
        CampaignResponse response = campaignService.getCampaignById(id);
        return ResponseEntity.ok(ApiResponse.success("Relief campaign retrieved successfully", response));
    }

    /**
     * PATCH /api/campaigns/{id}/status — Update campaign status (REQ-CAMP-3, REQ-CAMP-4).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CampaignResponse>> updateCampaignStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CampaignStatusUpdateRequest request) {

        CampaignResponse response = campaignService.updateCampaignStatus(id, request, principal);
        return ResponseEntity.ok(ApiResponse.success("Relief campaign status updated successfully", response));
    }

    /**
     * PATCH /api/campaigns/{id}/verify — Admin moderation of campaign verification.
     */
    @PatchMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<CampaignResponse>> verifyCampaign(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam VerificationStatus status) {

        CampaignResponse response = campaignService.verifyCampaign(id, status, principal);
        return ResponseEntity.ok(ApiResponse.success("Relief campaign verification updated successfully", response));
    }
}
