package com.disasterconnect.controller;

import com.disasterconnect.dto.ApiResponse;
import com.disasterconnect.dto.ShelterCreateRequest;
import com.disasterconnect.dto.ShelterResponse;
import com.disasterconnect.dto.ShelterUpdateRequest;
import com.disasterconnect.entity.enums.ServiceType;
import com.disasterconnect.entity.enums.ShelterStatus;
import com.disasterconnect.security.UserPrincipal;
import com.disasterconnect.service.ShelterService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/shelters", "/api/services"})
public class ShelterController {

    private final ShelterService shelterService;

    public ShelterController(ShelterService shelterService) {
        this.shelterService = shelterService;
    }

    /**
     * POST /api/shelters — Authority and Admin add a new shelter/essential service facility per REQ-SVC-3.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ShelterResponse>> createShelter(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ShelterCreateRequest request) {

        ShelterResponse response = shelterService.createShelter(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Shelter/Essential service created successfully", response));
    }

    /**
     * GET /api/shelters — Directory of shelters and essential services per REQ-SVC-1 and REQ-SVC-4.
     * Publicly accessible; filterable by type, status, city.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ShelterResponse>>> getShelters(
            @RequestParam(required = false) ServiceType type,
            @RequestParam(required = false) ShelterStatus status,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ShelterResponse> responses = shelterService.getShelters(type, status, city, page, size);
        return ResponseEntity.ok(ApiResponse.success("Shelters and essential services retrieved successfully", responses));
    }

    /**
     * GET /api/shelters/nearby — Nearby service discovery sorted by distance using Haversine formula per REQ-SVC-2.
     * Accessible publicly and to citizens.
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<ShelterResponse>>> getNearbyShelters(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(required = false) ServiceType type,
            @RequestParam(required = false) ShelterStatus status,
            @RequestParam(defaultValue = "20") int limit) {

        List<ShelterResponse> responses = shelterService.findNearby(latitude, longitude, radiusKm, type, status, limit);
        return ResponseEntity.ok(ApiResponse.success("Nearby shelters and essential services retrieved successfully", responses));
    }

    /**
     * GET /api/shelters/{id} — Retrieve facility details by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShelterResponse>> getShelterById(@PathVariable Long id) {
        ShelterResponse response = shelterService.getShelterById(id);
        return ResponseEntity.ok(ApiResponse.success("Shelter details retrieved successfully", response));
    }

    /**
     * PUT/PATCH /api/shelters/{id} — Authority or Admin updates facility details or capacity per REQ-SVC-3.
     */
    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<ShelterResponse>> updateShelter(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ShelterUpdateRequest request) {

        ShelterResponse response = shelterService.updateShelter(id, principal, request);
        return ResponseEntity.ok(ApiResponse.success("Shelter/Essential service updated successfully", response));
    }

    /**
     * DELETE /api/shelters/{id} or PATCH /api/shelters/{id}/deactivate — Deactivate facility per REQ-SVC-3.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ShelterResponse>> deleteOrDeactivateShelter(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        ShelterResponse response = shelterService.deactivateShelter(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Shelter/Essential service deactivated successfully", response));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<ShelterResponse>> deactivateShelter(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        ShelterResponse response = shelterService.deactivateShelter(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Shelter/Essential service deactivated successfully", response));
    }
}
