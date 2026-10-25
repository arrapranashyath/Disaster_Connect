package com.disasterconnect.service;

import com.disasterconnect.dto.ResourceCreateRequest;
import com.disasterconnect.dto.ResourceResponse;
import com.disasterconnect.dto.ResourceUpdateRequest;
import com.disasterconnect.entity.NGOProfile;
import com.disasterconnect.entity.ReliefResource;
import com.disasterconnect.entity.ResourceUpdateLog;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.enums.RequestPriority;
import com.disasterconnect.entity.enums.ResourceStatus;
import com.disasterconnect.entity.enums.ResourceType;
import com.disasterconnect.entity.enums.Role;
import com.disasterconnect.entity.enums.VerificationStatus;
import com.disasterconnect.exception.BadRequestException;
import com.disasterconnect.exception.ResourceNotFoundException;
import com.disasterconnect.repository.NGOProfileRepository;
import com.disasterconnect.repository.ReliefResourceRepository;
import com.disasterconnect.repository.ResourceUpdateLogRepository;
import com.disasterconnect.repository.UserRepository;
import com.disasterconnect.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    private final ReliefResourceRepository resourceRepository;
    private final ResourceUpdateLogRepository logRepository;
    private final UserRepository userRepository;
    private final NGOProfileRepository ngoProfileRepository;

    public ResourceService(ReliefResourceRepository resourceRepository,
                           ResourceUpdateLogRepository logRepository,
                           UserRepository userRepository,
                           NGOProfileRepository ngoProfileRepository) {
        this.resourceRepository = resourceRepository;
        this.logRepository = logRepository;
        this.userRepository = userRepository;
        this.ngoProfileRepository = ngoProfileRepository;
    }

    /**
     * Record a new relief resource entry per REQ-RES-1.
     * Allowed for verified NGOs and Admins.
     */
    @Transactional
    public ResourceResponse createResource(UserPrincipal principal, ResourceCreateRequest request) {
        User user = getUserOrThrow(principal.getUserId());
        NGOProfile ngoProfile = null;

        if (principal.getRole() == Role.NGO) {
            ngoProfile = validateAndGetVerifiedNgo(user);
        } else if (principal.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Access denied: only verified NGOs and Administrators can record resources");
        }

        ReliefResource resource = new ReliefResource(
                ngoProfile,
                request.getResourceType(),
                request.getRequiredQuantity(),
                request.getAvailableQuantity(),
                request.getUnit(),
                request.getLocation(),
                request.getPriority(),
                request.getCriticalThresholdPercent(),
                request.getLowThresholdPercent()
        );

        ReliefResource saved = resourceRepository.save(resource);

        // Record initial log entry per REQ-RES-4
        String logNote = (request.getNotes() != null && !request.getNotes().isBlank())
                ? request.getNotes() : "Initial resource entry recorded";
        ResourceUpdateLog initialLog = new ResourceUpdateLog(
                saved, user, null, saved.getAvailableQuantity(),
                null, saved.getStatus(), logNote
        );
        logRepository.save(initialLog);

        return toResponse(saved, false);
    }

    /**
     * Update resource quantities and details per REQ-RES-1 and REQ-RES-3.
     * Allowed for owning verified NGO or Admin.
     */
    @Transactional
    public ResourceResponse updateResource(Long id, UserPrincipal principal, ResourceUpdateRequest request) {
        User user = getUserOrThrow(principal.getUserId());
        ReliefResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relief resource not found with id: " + id));

        // Ownership/permission check
        if (principal.getRole() == Role.NGO) {
            NGOProfile ngoProfile = validateAndGetVerifiedNgo(user);
            if (resource.getProvidedBy() == null || !resource.getProvidedBy().getNgoId().equals(ngoProfile.getNgoId())) {
                throw new AccessDeniedException("Access denied: NGOs can only update their own resource entries");
            }
        } else if (principal.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Access denied: only the owning verified NGO or an Admin can update resources");
        }

        Double prevQuantity = resource.getAvailableQuantity();
        ResourceStatus prevStatus = resource.getStatus();

        if (request.getResourceType() != null) {
            resource.setResourceType(request.getResourceType());
        }
        if (request.getRequiredQuantity() != null) {
            resource.setRequiredQuantity(request.getRequiredQuantity());
        }
        if (request.getAvailableQuantity() != null) {
            resource.setAvailableQuantity(request.getAvailableQuantity());
        }
        if (request.getUnit() != null && !request.getUnit().isBlank()) {
            resource.setUnit(request.getUnit());
        }
        if (request.getLocation() != null && !request.getLocation().isBlank()) {
            resource.setLocation(request.getLocation());
        }
        if (request.getPriority() != null) {
            resource.setPriority(request.getPriority());
        }
        if (request.getCriticalThresholdPercent() != null) {
            resource.setCriticalThresholdPercent(request.getCriticalThresholdPercent());
        }
        if (request.getLowThresholdPercent() != null) {
            resource.setLowThresholdPercent(request.getLowThresholdPercent());
        }

        resource.recalculateStatus();
        ReliefResource updated = resourceRepository.save(resource);

        // Record history log per REQ-RES-4
        String logNote = (request.getNotes() != null && !request.getNotes().isBlank())
                ? request.getNotes() : "Resource updated";
        ResourceUpdateLog log = new ResourceUpdateLog(
                updated, user, prevQuantity, updated.getAvailableQuantity(),
                prevStatus, updated.getStatus(), logNote
        );
        logRepository.save(log);

        return toResponse(updated, true);
    }

    /**
     * Search and list resources with optional filters.
     * Accessible by NGOs, Authorities, Admins, and Citizens per SRS Section 3.7.
     */
    @Transactional(readOnly = true)
    public Page<ResourceResponse> searchResources(ResourceType type, ResourceStatus status,
                                                  RequestPriority priority, String location,
                                                  int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return resourceRepository.searchResources(type, status, priority, location, pageable)
                .map(r -> toResponse(r, false));
    }

    /**
     * View resource status by area to identify shortages per REQ-RES-2.
     * Returns all matching resources in an area with shortage status.
     */
    @Transactional(readOnly = true)
    public List<ResourceResponse> getResourcesByArea(String area, boolean shortagesOnly) {
        if (area == null || area.isBlank()) {
            throw new BadRequestException("Area name/location is required");
        }

        List<ReliefResource> list;
        if (shortagesOnly) {
            list = resourceRepository.findShortagesByArea(area);
        } else {
            list = resourceRepository.findAllByLocationContainingIgnoreCase(area);
        }

        return list.stream()
                .map(r -> toResponse(r, false))
                .collect(Collectors.toList());
    }

    /**
     * Get single resource with full update history per REQ-RES-4.
     */
    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id) {
        ReliefResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relief resource not found with id: " + id));
        return toResponse(resource, true);
    }

    /**
     * Delete resource entry (only owning NGO or Admin).
     */
    @Transactional
    public void deleteResource(Long id, UserPrincipal principal) {
        User user = getUserOrThrow(principal.getUserId());
        ReliefResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relief resource not found with id: " + id));

        if (principal.getRole() == Role.NGO) {
            NGOProfile ngoProfile = validateAndGetVerifiedNgo(user);
            if (resource.getProvidedBy() == null || !resource.getProvidedBy().getNgoId().equals(ngoProfile.getNgoId())) {
                throw new AccessDeniedException("Access denied: NGOs can only delete their own resource entries");
            }
        } else if (principal.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Access denied: only the owning verified NGO or an Admin can delete resources");
        }

        resourceRepository.delete(resource);
    }

    // -----------------------------------------------------------------------
    // Helper validations
    // -----------------------------------------------------------------------

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private NGOProfile validateAndGetVerifiedNgo(User user) {
        NGOProfile profile = ngoProfileRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("NGO profile not found for user."));
        if (profile.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new BadRequestException("NGO account is pending verification. Only verified NGOs can record relief resources.");
        }
        return profile;
    }

    public ResourceResponse toResponse(ReliefResource resource, boolean includeHistory) {
        ResourceResponse res = new ResourceResponse();
        res.setResourceId(resource.getResourceId());
        if (resource.getProvidedBy() != null) {
            res.setNgoId(resource.getProvidedBy().getNgoId());
            res.setOrganizationName(resource.getProvidedBy().getOrgName());
        }
        res.setResourceType(resource.getResourceType());
        res.setRequiredQuantity(resource.getRequiredQuantity());
        res.setAvailableQuantity(resource.getAvailableQuantity());

        double req = resource.getRequiredQuantity() != null ? resource.getRequiredQuantity() : 0.0;
        double avail = resource.getAvailableQuantity() != null ? resource.getAvailableQuantity() : 0.0;
        double deficit = Math.max(0.0, req - avail);
        res.setDeficitQuantity(deficit);

        res.setUnit(resource.getUnit());
        res.setLocation(resource.getLocation());
        res.setPriority(resource.getPriority());
        res.setStatus(resource.getStatus());
        res.setCriticalThresholdPercent(resource.getCriticalThresholdPercent());
        res.setLowThresholdPercent(resource.getLowThresholdPercent());
        res.setCreatedAt(resource.getCreatedAt());
        res.setUpdatedAt(resource.getUpdatedAt());

        if (includeHistory) {
            List<ResourceUpdateLog> logs = logRepository.findAllByResourceOrderByCreatedAtAsc(resource);
            List<ResourceResponse.UpdateLogEntry> history = logs.stream()
                    .map(l -> new ResourceResponse.UpdateLogEntry(
                            l.getLogId(),
                            l.getUpdatedBy() != null ? l.getUpdatedBy().getUserId() : null,
                            l.getUpdatedBy() != null ? l.getUpdatedBy().getName() : "System",
                            l.getPreviousQuantity(),
                            l.getNewQuantity(),
                            l.getPreviousStatus(),
                            l.getNewStatus(),
                            l.getNotes(),
                            l.getCreatedAt()
                    ))
                    .collect(Collectors.toList());
            res.setUpdateHistory(history);
        }

        return res;
    }
}
