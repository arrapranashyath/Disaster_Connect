package com.disasterconnect.service;

import com.disasterconnect.dto.IncidentCreateRequest;
import com.disasterconnect.dto.IncidentResponse;
import com.disasterconnect.dto.IncidentStatusUpdateRequest;
import com.disasterconnect.entity.IncidentReport;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.enums.Role;
import com.disasterconnect.exception.BadRequestException;
import com.disasterconnect.exception.ResourceNotFoundException;
import com.disasterconnect.repository.IncidentReportRepository;
import com.disasterconnect.repository.UserRepository;
import com.disasterconnect.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentService {

    private final IncidentReportRepository incidentRepository;
    private final UserRepository userRepository;

    public IncidentService(IncidentReportRepository incidentRepository, UserRepository userRepository) {
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Submit a new incident report. The reporter is derived from the authenticated
     * principal — never from a client-supplied field.
     */
    @Transactional
    public IncidentResponse submitIncident(UserPrincipal principal, IncidentCreateRequest request) {
        User reporter = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        IncidentReport report = new IncidentReport();
        report.setReportedBy(reporter);
        report.setIncidentType(request.getIncidentType());
        report.setDescription(request.getDescription());
        report.setLocationName(request.getLocationName());
        report.setLatitude(request.getLatitude());
        report.setLongitude(request.getLongitude());
        report.setSeverity(request.getSeverity());
        report.setImageUrl(request.getImageUrl());

        IncidentReport saved = incidentRepository.save(report);
        return toResponse(saved);
    }

    /**
     * Return only the incidents submitted by the currently authenticated citizen.
     */
    @Transactional(readOnly = true)
    public Page<IncidentResponse> getMyIncidents(UserPrincipal principal, int page, int size) {
        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        User reporter = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return incidentRepository.findAllByReportedByOrderByCreatedAtDesc(reporter, pageable)
                .map(this::toResponse);
    }

    /**
     * Return all incidents — for AUTHORITY, ADMIN, and NGO users.
     */
    @Transactional(readOnly = true)
    public Page<IncidentResponse> getAllIncidents(int page, int size) {
        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return incidentRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    /**
     * Return a single incident by ID.
     * Access control (citizen can only see own, others can see all) is enforced in the controller.
     */
    @Transactional(readOnly = true)
    public IncidentResponse getIncidentById(Long incidentId, UserPrincipal principal) {
        IncidentReport report = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + incidentId));

        // Citizens may only view their own incidents
        boolean isCitizen = Role.CITIZEN.equals(principal.getRole());
        if (isCitizen && !report.getReportedBy().getUserId().equals(principal.getUserId())) {
            throw new AccessDeniedException("Access denied: you may only view your own incidents");
        }

        return toResponse(report);
    }

    /**
     * Update the status of an incident. Only AUTHORITY and ADMIN may call this.
     */
    @Transactional
    public IncidentResponse updateIncidentStatus(Long incidentId, IncidentStatusUpdateRequest request) {
        IncidentReport report = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + incidentId));
        report.setStatus(request.getStatus());
        IncidentReport updated = incidentRepository.save(report);
        return toResponse(updated);
    }

    // ---------------------------------------------------------------------------
    // Mapping helper
    // ---------------------------------------------------------------------------

    private IncidentResponse toResponse(IncidentReport report) {
        IncidentResponse response = new IncidentResponse();
        response.setIncidentId(report.getIncidentId());
        response.setIncidentType(report.getIncidentType());
        response.setDescription(report.getDescription());
        response.setLocationName(report.getLocationName());
        response.setLatitude(report.getLatitude());
        response.setLongitude(report.getLongitude());
        response.setSeverity(report.getSeverity());
        response.setStatus(report.getStatus());
        response.setImageUrl(report.getImageUrl());
        response.setCreatedAt(report.getCreatedAt());
        response.setUpdatedAt(report.getUpdatedAt());

        User reporter = report.getReportedBy();
        response.setReportedBy(new IncidentResponse.ReportedByInfo(
                reporter.getUserId(),
                reporter.getName(),
                reporter.getRole().name()
        ));

        return response;
    }
}
