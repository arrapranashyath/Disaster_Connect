package com.disasterconnect.service;

import com.disasterconnect.dto.ShelterCreateRequest;
import com.disasterconnect.dto.ShelterResponse;
import com.disasterconnect.dto.ShelterUpdateRequest;
import com.disasterconnect.entity.Shelter;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.enums.Role;
import com.disasterconnect.entity.enums.ServiceType;
import com.disasterconnect.entity.enums.ShelterStatus;
import com.disasterconnect.exception.BadRequestException;
import com.disasterconnect.exception.ResourceNotFoundException;
import com.disasterconnect.repository.ShelterRepository;
import com.disasterconnect.repository.UserRepository;
import com.disasterconnect.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShelterService {

    private final ShelterRepository shelterRepository;
    private final UserRepository userRepository;

    public ShelterService(ShelterRepository shelterRepository, UserRepository userRepository) {
        this.shelterRepository = shelterRepository;
        this.userRepository = userRepository;
    }

    /**
     * Add a new shelter or essential service. Restricted to AUTHORITY and ADMIN per REQ-SVC-3.
     */
    @Transactional
    public ShelterResponse createShelter(UserPrincipal principal, ShelterCreateRequest request) {
        validateAuthorityOrAdmin(principal);

        User creator = null;
        if (principal != null && principal.getUserId() != null) {
            creator = userRepository.findById(principal.getUserId()).orElse(null);
        }

        if (request.getCapacity() != null && request.getAvailableSpace() != null) {
            if (request.getAvailableSpace() > request.getCapacity()) {
                throw new BadRequestException("Available space cannot exceed total capacity");
            }
        }

        Shelter shelter = new Shelter(
                request.getName(),
                request.getServiceType(),
                request.getAddress(),
                request.getCity(),
                request.getLatitude(),
                request.getLongitude(),
                request.getCapacity(),
                request.getAvailableSpace(),
                request.getContactPhone(),
                request.getContactEmail(),
                request.getStatus(),
                creator
        );

        Shelter saved = shelterRepository.save(shelter);
        return toResponse(saved, null);
    }

    /**
     * Search/List directory of shelters and services with optional type, status, city filters.
     * Accessible by public and all authenticated users per REQ-SVC-1 and REQ-SVC-4.
     */
    @Transactional(readOnly = true)
    public Page<ShelterResponse> getShelters(ServiceType type, ShelterStatus status, String city, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return shelterRepository.searchShelters(type, status, city, pageable)
                .map(s -> toResponse(s, null));
    }

    /**
     * Get details for a single shelter/service facility.
     */
    @Transactional(readOnly = true)
    public ShelterResponse getShelterById(Long id) {
        Shelter shelter = shelterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shelter/Service not found with id: " + id));
        return toResponse(shelter, null);
    }

    /**
     * Find nearby shelters and services sorted by distance using Haversine formula per REQ-SVC-2.
     * Allows filtering by type and status, default max radius or count.
     */
    @Transactional(readOnly = true)
    public List<ShelterResponse> findNearby(Double latitude, Double longitude, Double radiusKm,
                                            ServiceType type, ShelterStatus status, int limit) {
        if (latitude == null || longitude == null) {
            throw new BadRequestException("Both latitude and longitude are required for nearby service discovery");
        }
        if (latitude < -90.0 || latitude > 90.0 || longitude < -180.0 || longitude > 180.0) {
            throw new BadRequestException("Invalid latitude or longitude coordinates");
        }

        int safeLimit = Math.min(Math.max(limit, 1), 50);
        Pageable pageable = PageRequest.of(0, safeLimit);

        List<Object[]> results = shelterRepository.findNearbyWithDistance(latitude, longitude, type, status, pageable);
        List<ShelterResponse> responses = new ArrayList<>();

        for (Object[] row : results) {
            Shelter s = (Shelter) row[0];
            Number distNum = (Number) row[1];
            double dist = distNum != null ? Math.round(distNum.doubleValue() * 100.0) / 100.0 : 0.0;

            if (radiusKm == null || dist <= radiusKm) {
                responses.add(toResponse(s, dist));
            }
        }

        return responses;
    }

    /**
     * Update shelter/service details or capacity. Restricted to AUTHORITY and ADMIN per REQ-SVC-3.
     */
    @Transactional
    public ShelterResponse updateShelter(Long id, UserPrincipal principal, ShelterUpdateRequest request) {
        validateAuthorityOrAdmin(principal);

        Shelter shelter = shelterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shelter/Service not found with id: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            shelter.setName(request.getName());
        }
        if (request.getServiceType() != null) {
            shelter.setServiceType(request.getServiceType());
        }
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            shelter.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            shelter.setCity(request.getCity());
        }
        if (request.getLatitude() != null) {
            shelter.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            shelter.setLongitude(request.getLongitude());
        }
        if (request.getCapacity() != null) {
            shelter.setCapacity(request.getCapacity());
        }
        if (request.getAvailableSpace() != null) {
            shelter.setAvailableSpace(request.getAvailableSpace());
        }
        if (request.getContactPhone() != null) {
            shelter.setContactPhone(request.getContactPhone());
        }
        if (request.getContactEmail() != null) {
            shelter.setContactEmail(request.getContactEmail());
        }
        if (request.getStatus() != null) {
            shelter.setStatus(request.getStatus());
        }

        if (shelter.getCapacity() != null && shelter.getAvailableSpace() != null) {
            if (shelter.getAvailableSpace() > shelter.getCapacity()) {
                throw new BadRequestException("Available space cannot exceed total capacity");
            }
        }

        Shelter updated = shelterRepository.save(shelter);
        return toResponse(updated, null);
    }

    /**
     * Deactivate shelter/service facility per REQ-SVC-3.
     */
    @Transactional
    public ShelterResponse deactivateShelter(Long id, UserPrincipal principal) {
        validateAuthorityOrAdmin(principal);

        Shelter shelter = shelterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shelter/Service not found with id: " + id));

        shelter.setStatus(ShelterStatus.INACTIVE);
        Shelter updated = shelterRepository.save(shelter);
        return toResponse(updated, null);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void validateAuthorityOrAdmin(UserPrincipal principal) {
        if (principal == null || (principal.getRole() != Role.AUTHORITY && principal.getRole() != Role.ADMIN)) {
            throw new AccessDeniedException("Access denied: only Authority or Administrator accounts may manage shelters and essential services");
        }
    }

    public ShelterResponse toResponse(Shelter shelter, Double distanceKm) {
        ShelterResponse res = new ShelterResponse();
        res.setShelterId(shelter.getShelterId());
        res.setName(shelter.getName());
        res.setServiceType(shelter.getServiceType());
        res.setAddress(shelter.getAddress());
        res.setCity(shelter.getCity());
        res.setLatitude(shelter.getLatitude());
        res.setLongitude(shelter.getLongitude());
        res.setCapacity(shelter.getCapacity());
        res.setAvailableSpace(shelter.getAvailableSpace());
        res.setContactPhone(shelter.getContactPhone());
        res.setContactEmail(shelter.getContactEmail());
        res.setStatus(shelter.getStatus());
        res.setDistanceKm(distanceKm);
        if (shelter.getCreatedBy() != null) {
            res.setCreatedById(shelter.getCreatedBy().getUserId());
            res.setCreatedByName(shelter.getCreatedBy().getName());
        }
        res.setCreatedAt(shelter.getCreatedAt());
        res.setUpdatedAt(shelter.getUpdatedAt());
        return res;
    }
}
