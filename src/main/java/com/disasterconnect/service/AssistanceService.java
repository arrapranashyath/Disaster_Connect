package com.disasterconnect.service;

import com.disasterconnect.dto.*;
import com.disasterconnect.entity.*;
import com.disasterconnect.entity.enums.*;
import com.disasterconnect.exception.BadRequestException;
import com.disasterconnect.exception.ResourceNotFoundException;
import com.disasterconnect.repository.*;
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
public class AssistanceService {

    private final AssistanceRequestRepository assistanceRepository;
    private final RequestStatusLogRepository statusLogRepository;
    private final UserRepository userRepository;
    private final NGOProfileRepository ngoProfileRepository;
    private final VolunteerProfileRepository volunteerProfileRepository;
    private final NotificationService notificationService;

    public AssistanceService(AssistanceRequestRepository assistanceRepository,
                             RequestStatusLogRepository statusLogRepository,
                             UserRepository userRepository,
                             NGOProfileRepository ngoProfileRepository,
                             VolunteerProfileRepository volunteerProfileRepository,
                             NotificationService notificationService) {
        this.assistanceRepository = assistanceRepository;
        this.statusLogRepository = statusLogRepository;
        this.userRepository = userRepository;
        this.ngoProfileRepository = ngoProfileRepository;
        this.volunteerProfileRepository = volunteerProfileRepository;
        this.notificationService = notificationService;
    }

    /**
     * Create an emergency assistance request. The citizen is derived directly
     * from the authenticated principal — client-supplied user IDs are never trusted.
     */
    @Transactional
    public AssistanceResponse createRequest(UserPrincipal principal, AssistanceCreateRequest request) {
        User citizen = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        AssistanceRequest entity = new AssistanceRequest(
                citizen,
                request.getAssistanceType(),
                request.getDescription(),
                request.getLocationName(),
                request.getLatitude(),
                request.getLongitude(),
                request.getPeopleCount(),
                request.getPriority()
        );

        AssistanceRequest saved = assistanceRepository.save(entity);

        // Record initial status audit log per REQ-HELP-6
        RequestStatusLog log = new RequestStatusLog(saved, citizen, null, RequestStatus.PENDING, "Request created");
        statusLogRepository.save(log);

        return toResponse(saved, false);
    }

    /**
     * Retrieve requests based on caller's role and filters.
     * Enforces role checks:
     * - Citizens only see their own requests.
     * - Volunteers must be verified to view open requests.
     * - NGOs must be verified to view open requests.
     * - Authorities and Admins see all requests.
     */
    @Transactional(readOnly = true)
    public Page<AssistanceResponse> getRequests(UserPrincipal principal,
                                                RequestStatus status,
                                                AssistanceType type,
                                                RequestPriority priority,
                                                String location,
                                                int page,
                                                int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Role role = principal.getRole();

        // 1. Citizen role constraint: only own requests
        if (role == Role.CITIZEN) {
            User citizen = userRepository.findById(principal.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            return assistanceRepository.findAllByCitizenOrderByCreatedAtDesc(citizen, pageable)
                    .map(r -> toResponse(r, false));
        }

        // 2. Volunteer verification check
        if (role == Role.VOLUNTEER) {
            validateVolunteerVerified(principal.getUserId());
        }

        // 3. NGO verification check
        if (role == Role.NGO) {
            validateNgoVerified(principal.getUserId());
        }

        // 4. Query with optional filters
        return assistanceRepository.searchRequests(status, type, priority, location, pageable)
                .map(r -> toResponse(r, false));
    }

    /**
     * Retrieve only the current authenticated citizen's submitted requests.
     */
    @Transactional(readOnly = true)
    public Page<AssistanceResponse> getMyRequests(UserPrincipal principal, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        User citizen = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return assistanceRepository.findAllByCitizenOrderByCreatedAtDesc(citizen, pageable)
                .map(r -> toResponse(r, false));
    }

    /**
     * Retrieve requests currently assigned to the calling volunteer or NGO responder.
     */
    @Transactional(readOnly = true)
    public Page<AssistanceResponse> getAssignedToMe(UserPrincipal principal, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (principal.getRole() == Role.VOLUNTEER) {
            validateVolunteerVerified(principal.getUserId());
        } else if (principal.getRole() == Role.NGO) {
            validateNgoVerified(principal.getUserId());
        }

        User responder = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return assistanceRepository.findAllByAssignedToOrderByCreatedAtDesc(responder, pageable)
                .map(r -> toResponse(r, false));
    }

    /**
     * Get a single assistance request by ID with full audit history.
     * Enforces ownership for citizens.
     */
    @Transactional(readOnly = true)
    public AssistanceResponse getRequestById(Long requestId, UserPrincipal principal) {
        AssistanceRequest request = assistanceRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Assistance request not found with id: " + requestId));

        // Citizens may only view their own requests
        if (principal.getRole() == Role.CITIZEN && !request.getCitizen().getUserId().equals(principal.getUserId())) {
            throw new AccessDeniedException("Access denied: you may only view your own help requests");
        }

        // Volunteers must be verified to inspect details
        if (principal.getRole() == Role.VOLUNTEER) {
            validateVolunteerVerified(principal.getUserId());
        } else if (principal.getRole() == Role.NGO) {
            validateNgoVerified(principal.getUserId());
        }

        return toResponse(request, true);
    }

    /**
     * Accept an open help request (by verified volunteer or verified NGO).
     * Enforces REQ-COOR-4: Prevents the same request from being simultaneously accepted.
     */
    @Transactional
    public AssistanceResponse acceptRequest(Long requestId, UserPrincipal principal) {
        User responder = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (principal.getRole() == Role.VOLUNTEER) {
            validateVolunteerVerified(principal.getUserId());
        } else if (principal.getRole() == Role.NGO) {
            validateNgoVerified(principal.getUserId());
        } else if (principal.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only verified volunteers, NGOs, or admins may accept requests");
        }

        AssistanceRequest request = assistanceRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Assistance request not found with id: " + requestId));

        // Mutual exclusion check per REQ-COOR-4
        if (request.getStatus() != RequestStatus.PENDING || request.getAssignedTo() != null) {
            throw new BadRequestException("This request has already been accepted or assigned to another responder.");
        }

        RequestStatus previousStatus = request.getStatus();
        request.setStatus(RequestStatus.ACCEPTED);
        request.setAssignedTo(responder);
        request.setAssignedBy(responder);

        AssistanceRequest updated = assistanceRepository.save(request);

        // Audit log
        RequestStatusLog log = new RequestStatusLog(updated, responder, previousStatus,
                RequestStatus.ACCEPTED, "Request accepted by " + responder.getName());
        statusLogRepository.save(log);

        // REQ-NOTIF-2: Notify citizen
        notificationService.notifyHelpRequestStatusChange(
                updated.getCitizen(),
                "Help Request Accepted",
                "Your help request was accepted by " + responder.getName(),
                updated.getRequestId()
        );

        return toResponse(updated, true);
    }

    /**
     * NGO or Admin assigns a verified volunteer to a help request per REQ-COOR-2.
     */
    @Transactional
    public AssistanceResponse assignVolunteer(Long requestId, UserPrincipal principal, AssignVolunteerRequest assignReq) {
        User coordinator = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Coordinator not found"));

        if (principal.getRole() == Role.NGO) {
            validateNgoVerified(principal.getUserId());
        } else if (principal.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only verified NGOs or administrators may assign volunteers");
        }

        AssistanceRequest request = assistanceRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Assistance request not found with id: " + requestId));

        // Check if request is still open / re-assignable
        if (request.getStatus().isTerminal()) {
            throw new BadRequestException("Cannot assign volunteer to a closed/terminal request.");
        }

        // Look up target volunteer user
        User targetVolunteer = userRepository.findById(assignReq.getVolunteerUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer user not found with id: " + assignReq.getVolunteerUserId()));

        if (targetVolunteer.getRole() != Role.VOLUNTEER) {
            throw new BadRequestException("Assigned user must have the VOLUNTEER role.");
        }

        // Target volunteer must be VERIFIED
        validateVolunteerVerified(targetVolunteer.getUserId());

        RequestStatus previousStatus = request.getStatus();
        request.setStatus(RequestStatus.ASSIGNED);
        request.setAssignedTo(targetVolunteer);
        request.setAssignedBy(coordinator);

        AssistanceRequest updated = assistanceRepository.save(request);

        String note = (assignReq.getNotes() != null && !assignReq.getNotes().isBlank())
                ? assignReq.getNotes()
                : "Assigned to volunteer " + targetVolunteer.getName() + " by " + coordinator.getName();

        RequestStatusLog log = new RequestStatusLog(updated, coordinator, previousStatus, RequestStatus.ASSIGNED, note);
        statusLogRepository.save(log);

        // REQ-NOTIF-2: Notify citizen
        notificationService.notifyHelpRequestStatusChange(
                updated.getCitizen(),
                "Volunteer Assigned",
                "Volunteer " + targetVolunteer.getName() + " was assigned to your help request",
                updated.getRequestId()
        );

        return toResponse(updated, true);
    }

    /**
     * Update request status (e.g. IN_PROGRESS, COMPLETED, RESOLVED).
     * Enforces that only the assigned responder, assigning NGO, authority, or admin can update.
     */
    @Transactional
    public AssistanceResponse updateRequestStatus(Long requestId, UserPrincipal principal, AssistanceStatusUpdateRequest updateReq) {
        User updater = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        AssistanceRequest request = assistanceRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Assistance request not found with id: " + requestId));

        // Check terminal state
        if (request.getStatus().isTerminal()) {
            throw new BadRequestException("Cannot change status of a completed or cancelled request.");
        }

        // Access check: only assignee, coordinator, authority, or admin
        boolean isAssignee = request.getAssignedTo() != null && request.getAssignedTo().getUserId().equals(principal.getUserId());
        boolean isAssigner = request.getAssignedBy() != null && request.getAssignedBy().getUserId().equals(principal.getUserId());
        boolean isPrivileged = principal.getRole() == Role.ADMIN || principal.getRole() == Role.AUTHORITY;

        if (!isAssignee && !isAssigner && !isPrivileged) {
            throw new AccessDeniedException("Access denied: you are not authorized to update this request's status");
        }

        RequestStatus previousStatus = request.getStatus();
        RequestStatus newStatus = updateReq.getStatus();

        // Validate transition
        if (newStatus == RequestStatus.PENDING && !isPrivileged) {
            throw new BadRequestException("Cannot revert request status to PENDING.");
        }

        request.setStatus(newStatus);
        AssistanceRequest updated = assistanceRepository.save(request);

        String note = (updateReq.getNotes() != null && !updateReq.getNotes().isBlank())
                ? updateReq.getNotes()
                : "Status changed to " + newStatus;

        RequestStatusLog log = new RequestStatusLog(updated, updater, previousStatus, newStatus, note);
        statusLogRepository.save(log);

        // REQ-NOTIF-2: Notify citizen
        notificationService.notifyHelpRequestStatusChange(
                updated.getCitizen(),
                "Help Request Status Update",
                "Your help request status changed to " + newStatus,
                updated.getRequestId()
        );

        return toResponse(updated, true);
    }

    /**
     * Cancel an assistance request. Only the requesting citizen or admin may cancel.
     */
    @Transactional
    public AssistanceResponse cancelRequest(Long requestId, UserPrincipal principal, String reason) {
        User canceller = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        AssistanceRequest request = assistanceRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Assistance request not found with id: " + requestId));

        boolean isOwner = request.getCitizen().getUserId().equals(principal.getUserId());
        boolean isAdmin = principal.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Access denied: only the requester or an admin may cancel this request");
        }

        if (request.getStatus().isTerminal()) {
            throw new BadRequestException("Cannot cancel an already completed or cancelled request.");
        }

        RequestStatus prev = request.getStatus();
        request.setStatus(RequestStatus.CANCELLED);
        AssistanceRequest updated = assistanceRepository.save(request);

        String note = (reason != null && !reason.isBlank()) ? reason : "Cancelled by requester";
        RequestStatusLog log = new RequestStatusLog(updated, canceller, prev, RequestStatus.CANCELLED, note);
        statusLogRepository.save(log);

        // REQ-NOTIF-2: Notify citizen
        notificationService.notifyHelpRequestStatusChange(
                updated.getCitizen(),
                "Help Request Cancelled",
                "Your help request was cancelled",
                updated.getRequestId()
        );

        return toResponse(updated, true);
    }

    // -----------------------------------------------------------------------
    // Helper validations
    // -----------------------------------------------------------------------

    public void validateNgoVerified(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        NGOProfile profile = ngoProfileRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("NGO profile not found for user."));
        if (profile.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new BadRequestException("NGO account is pending verification. Only verified NGOs can coordinate requests.");
        }
    }

    public void validateVolunteerVerified(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        VolunteerProfile profile = volunteerProfileRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Volunteer profile not found for user."));
        if (profile.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new BadRequestException("Volunteer account is pending verification. Only verified volunteers can accept or coordinate requests.");
        }
    }

    // -----------------------------------------------------------------------
    // Response mapping
    // -----------------------------------------------------------------------

    private AssistanceResponse toResponse(AssistanceRequest request, boolean includeHistory) {
        AssistanceResponse res = new AssistanceResponse();
        res.setRequestId(request.getRequestId());
        res.setAssistanceType(request.getAssistanceType());
        res.setDescription(request.getDescription());
        res.setLocationName(request.getLocationName());
        res.setLatitude(request.getLatitude());
        res.setLongitude(request.getLongitude());
        res.setPeopleCount(request.getPeopleCount());
        res.setPriority(request.getPriority());
        res.setStatus(request.getStatus());
        res.setCreatedAt(request.getCreatedAt());
        res.setUpdatedAt(request.getUpdatedAt());

        // Citizen info
        User citizen = request.getCitizen();
        if (citizen != null) {
            res.setCitizen(new AssistanceResponse.UserSummary(
                    citizen.getUserId(),
                    citizen.getName(),
                    citizen.getRole().name(),
                    citizen.getPhone()
            ));
        }

        // Assigned to
        User assignedTo = request.getAssignedTo();
        if (assignedTo != null) {
            res.setAssignedTo(new AssistanceResponse.UserSummary(
                    assignedTo.getUserId(),
                    assignedTo.getName(),
                    assignedTo.getRole().name(),
                    assignedTo.getPhone()
            ));
        }

        // Assigned by
        User assignedBy = request.getAssignedBy();
        if (assignedBy != null) {
            res.setAssignedBy(new AssistanceResponse.UserSummary(
                    assignedBy.getUserId(),
                    assignedBy.getName(),
                    assignedBy.getRole().name(),
                    assignedBy.getPhone()
            ));
        }

        if (includeHistory) {
            List<RequestStatusLog> logs = statusLogRepository.findAllByRequestOrderByCreatedAtAsc(request);
            List<AssistanceResponse.StatusLogEntry> history = logs.stream()
                    .map(l -> new AssistanceResponse.StatusLogEntry(
                            l.getLogId(),
                            l.getChangedBy() != null ? l.getChangedBy().getName() : "System",
                            l.getPreviousStatus(),
                            l.getNewStatus(),
                            l.getNotes(),
                            l.getCreatedAt()
                    ))
                    .collect(Collectors.toList());
            res.setStatusHistory(history);
        }

        return res;
    }
}
