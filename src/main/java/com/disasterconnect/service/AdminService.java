package com.disasterconnect.service;

import com.disasterconnect.dto.*;
import com.disasterconnect.entity.AdminActionLog;
import com.disasterconnect.entity.NGOProfile;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.VolunteerProfile;
import com.disasterconnect.entity.enums.*;
import com.disasterconnect.exception.BadRequestException;
import com.disasterconnect.exception.ResourceNotFoundException;
import com.disasterconnect.repository.*;
import com.disasterconnect.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final NGOProfileRepository ngoProfileRepository;
    private final VolunteerProfileRepository volunteerProfileRepository;
    private final IncidentReportRepository incidentReportRepository;
    private final DisasterAlertRepository alertRepository;
    private final AssistanceRequestRepository assistanceRequestRepository;
    private final ReliefCampaignRepository campaignRepository;
    private final MissingPersonRepository missingPersonRepository;
    private final AdminActionLogRepository adminActionLogRepository;
    private final NotificationService notificationService;

    public AdminService(UserRepository userRepository,
                        NGOProfileRepository ngoProfileRepository,
                        VolunteerProfileRepository volunteerProfileRepository,
                        IncidentReportRepository incidentReportRepository,
                        DisasterAlertRepository alertRepository,
                        AssistanceRequestRepository assistanceRequestRepository,
                        ReliefCampaignRepository campaignRepository,
                        MissingPersonRepository missingPersonRepository,
                        AdminActionLogRepository adminActionLogRepository,
                        NotificationService notificationService) {
        this.userRepository = userRepository;
        this.ngoProfileRepository = ngoProfileRepository;
        this.volunteerProfileRepository = volunteerProfileRepository;
        this.incidentReportRepository = incidentReportRepository;
        this.alertRepository = alertRepository;
        this.assistanceRequestRepository = assistanceRequestRepository;
        this.campaignRepository = campaignRepository;
        this.missingPersonRepository = missingPersonRepository;
        this.adminActionLogRepository = adminActionLogRepository;
        this.notificationService = notificationService;
    }

    private void checkAdmin(UserPrincipal principal) {
        if (principal == null || principal.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only administrators can perform this action");
        }
    }

    /**
     * REQ-ADMIN-1: View pending NGO registrations.
     */
    @Transactional(readOnly = true)
    public Page<NgoVerificationResponse> getPendingNgos(UserPrincipal principal, Pageable pageable) {
        checkAdmin(principal);
        return ngoProfileRepository.findByVerificationStatus(VerificationStatus.PENDING, pageable)
                .map(NgoVerificationResponse::fromEntity);
    }

    /**
     * REQ-ADMIN-1: Approve or reject an NGO.
     */
    @Transactional
    public NgoVerificationResponse verifyNgo(Long ngoId, VerificationStatus status, UserPrincipal principal) {
        checkAdmin(principal);

        User admin = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        NGOProfile ngo = ngoProfileRepository.findById(ngoId)
                .orElseThrow(() -> new ResourceNotFoundException("NGO profile not found with id: " + ngoId));

        ngo.setVerificationStatus(status);
        NGOProfile updated = ngoProfileRepository.save(ngo);

        // Record administrative action log
        AdminActionLog log = new AdminActionLog(
                admin, "VERIFY_NGO", "NGO_PROFILE", ngoId, "Status updated to " + status
        );
        adminActionLogRepository.save(log);

        // Notify NGO user
        if (ngo.getUser() != null) {
            notificationService.createNotification(
                    ngo.getUser(), NotificationType.SYSTEM,
                    "NGO Verification " + status,
                    "Your organization (" + ngo.getOrgName() + ") verification status has been set to " + status,
                    "NGO_PROFILE", ngoId
            );
        }

        return NgoVerificationResponse.fromEntity(updated);
    }

    /**
     * View pending volunteers.
     */
    @Transactional(readOnly = true)
    public Page<VolunteerSummaryResponse> getPendingVolunteers(UserPrincipal principal, Pageable pageable) {
        checkAdmin(principal);
        return volunteerProfileRepository.findByVerificationStatus(VerificationStatus.PENDING, pageable)
                .map(vol -> new VolunteerSummaryResponse(
                        vol.getUser().getUserId(),
                        vol.getVolunteerId(),
                        vol.getUser().getName(),
                        vol.getUser().getEmail(),
                        vol.getUser().getPhone(),
                        vol.getSkills(),
                        vol.getAvailability(),
                        vol.getVerificationStatus(),
                        0
                ));
    }

    /**
     * Approve or reject a volunteer.
     */
    @Transactional
    public VolunteerSummaryResponse verifyVolunteer(Long volunteerId, VerificationStatus status, UserPrincipal principal) {
        checkAdmin(principal);

        User admin = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        VolunteerProfile vol = volunteerProfileRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer profile not found with id: " + volunteerId));

        vol.setVerificationStatus(status);
        VolunteerProfile updated = volunteerProfileRepository.save(vol);

        AdminActionLog log = new AdminActionLog(
                admin, "VERIFY_VOLUNTEER", "VOLUNTEER_PROFILE", volunteerId, "Status updated to " + status
        );
        adminActionLogRepository.save(log);

        if (vol.getUser() != null) {
            notificationService.createNotification(
                    vol.getUser(), NotificationType.SYSTEM,
                    "Volunteer Verification " + status,
                    "Your volunteer account verification status has been set to " + status,
                    "VOLUNTEER_PROFILE", volunteerId
            );
        }

        return new VolunteerSummaryResponse(
                updated.getUser().getUserId(),
                updated.getVolunteerId(),
                updated.getUser().getName(),
                updated.getUser().getEmail(),
                updated.getUser().getPhone(),
                updated.getSkills(),
                updated.getAvailability(),
                updated.getVerificationStatus(),
                0
        );
    }

    /**
     * REQ-ADMIN-3: Suspend or unsuspend a user account.
     */
    @Transactional
    public UserResponse suspendUser(Long userId, UserSuspendRequest request, UserPrincipal principal) {
        checkAdmin(principal);

        if (userId.equals(principal.getUserId())) {
            throw new BadRequestException("Administrator cannot suspend their own account");
        }

        User admin = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setSuspended(request.getSuspended());
        User updated = userRepository.save(user);

        String action = Boolean.TRUE.equals(request.getSuspended()) ? "SUSPEND_USER" : "UNSUSPEND_USER";
        AdminActionLog log = new AdminActionLog(admin, action, "USER", userId, request.getReason());
        adminActionLogRepository.save(log);

        return UserResponse.fromEntity(updated);
    }

    /**
     * REQ-ADMIN-2: System Overview Dashboard Summary.
     */
    @Transactional(readOnly = true)
    public AdminSummaryResponse getSystemSummary(UserPrincipal principal) {
        checkAdmin(principal);

        AdminSummaryResponse summary = new AdminSummaryResponse();
        summary.setTotalUsers(userRepository.count());
        summary.setTotalIncidents(incidentReportRepository.count());
        summary.setTotalAlerts(alertRepository.count());
        summary.setTotalAssistanceRequests(assistanceRequestRepository.count());

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (RequestStatus status : RequestStatus.values()) {
            statusCounts.put(status.name(), assistanceRequestRepository.countByStatus(status));
        }
        summary.setRequestsByStatus(statusCounts);
        summary.setPendingAssistanceRequests(statusCounts.getOrDefault("PENDING", 0L));

        summary.setTotalNgos(ngoProfileRepository.count());
        summary.setPendingNgos(ngoProfileRepository.findByVerificationStatus(VerificationStatus.PENDING).size());

        summary.setTotalVolunteers(volunteerProfileRepository.count());
        summary.setPendingVolunteers(volunteerProfileRepository.findByVerificationStatus(VerificationStatus.PENDING).size());

        summary.setActiveCampaigns(campaignRepository.findByStatusAndVerificationStatus(
                CampaignStatus.ACTIVE, VerificationStatus.VERIFIED).size());

        summary.setMissingPersonsCount(missingPersonRepository.findByStatus(MissingPersonStatus.MISSING).size());

        return summary;
    }

    /**
     * REQ-ADMIN-4: Retrieve administrative action history.
     */
    @Transactional(readOnly = true)
    public Page<AdminActionLogResponse> getAuditLogs(UserPrincipal principal, Pageable pageable) {
        checkAdmin(principal);
        return adminActionLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(AdminActionLogResponse::fromEntity);
    }
}
