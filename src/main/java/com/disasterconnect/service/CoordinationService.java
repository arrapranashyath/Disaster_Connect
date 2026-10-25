package com.disasterconnect.service;

import com.disasterconnect.dto.AssistanceResponse;
import com.disasterconnect.dto.CoordinationDashboardResponse;
import com.disasterconnect.dto.VolunteerSummaryResponse;
import com.disasterconnect.entity.AssistanceRequest;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.VolunteerProfile;
import com.disasterconnect.entity.enums.RequestStatus;
import com.disasterconnect.entity.enums.Role;
import com.disasterconnect.entity.enums.VerificationStatus;
import com.disasterconnect.exception.BadRequestException;
import com.disasterconnect.repository.AssistanceRequestRepository;
import com.disasterconnect.repository.UserRepository;
import com.disasterconnect.repository.VolunteerProfileRepository;
import com.disasterconnect.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CoordinationService {

    private final AssistanceRequestRepository assistanceRepository;
    private final AssistanceService assistanceService;
    private final VolunteerProfileRepository volunteerProfileRepository;
    private final UserRepository userRepository;

    public CoordinationService(AssistanceRequestRepository assistanceRepository,
                               AssistanceService assistanceService,
                               VolunteerProfileRepository volunteerProfileRepository,
                               UserRepository userRepository) {
        this.assistanceRepository = assistanceRepository;
        this.assistanceService = assistanceService;
        this.volunteerProfileRepository = volunteerProfileRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get NGO coordination dashboard per REQ-COOR-1.
     * Displays active requests, metrics, and available volunteers.
     */
    @Transactional(readOnly = true)
    public CoordinationDashboardResponse getDashboard(UserPrincipal principal) {
        if (principal.getRole() == Role.NGO) {
            assistanceService.validateNgoVerified(principal.getUserId());
        } else if (principal.getRole() != Role.ADMIN && principal.getRole() != Role.AUTHORITY) {
            throw new AccessDeniedException("Access denied: only NGOs, Authorities, or Admins may view coordination dashboard");
        }

        long pending = assistanceRepository.countByStatus(RequestStatus.PENDING);
        long assigned = assistanceRepository.countByStatus(RequestStatus.ASSIGNED)
                + assistanceRepository.countByStatus(RequestStatus.ACCEPTED);
        long inProgress = assistanceRepository.countByStatus(RequestStatus.IN_PROGRESS);
        long completed = assistanceRepository.countByStatus(RequestStatus.COMPLETED)
                + assistanceRepository.countByStatus(RequestStatus.RESOLVED);

        // Active requests: latest 20 open or in-progress requests
        Page<AssistanceRequest> activePage = assistanceRepository.searchRequests(
                null, null, null, null,
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        List<AssistanceResponse> activeRequests = activePage.getContent().stream()
                .filter(r -> !r.getStatus().isTerminal())
                .map(r -> {
                    AssistanceResponse res = new AssistanceResponse();
                    res.setRequestId(r.getRequestId());
                    res.setAssistanceType(r.getAssistanceType());
                    res.setDescription(r.getDescription());
                    res.setLocationName(r.getLocationName());
                    res.setPeopleCount(r.getPeopleCount());
                    res.setPriority(r.getPriority());
                    res.setStatus(r.getStatus());
                    res.setCreatedAt(r.getCreatedAt());
                    res.setUpdatedAt(r.getUpdatedAt());
                    if (r.getCitizen() != null) {
                        res.setCitizen(new AssistanceResponse.UserSummary(
                                r.getCitizen().getUserId(), r.getCitizen().getName(),
                                r.getCitizen().getRole().name(), r.getCitizen().getPhone()
                        ));
                    }
                    if (r.getAssignedTo() != null) {
                        res.setAssignedTo(new AssistanceResponse.UserSummary(
                                r.getAssignedTo().getUserId(), r.getAssignedTo().getName(),
                                r.getAssignedTo().getRole().name(), r.getAssignedTo().getPhone()
                        ));
                    }
                    return res;
                })
                .collect(Collectors.toList());

        List<VolunteerSummaryResponse> availableVolunteers = getAvailableVolunteers(principal);

        return new CoordinationDashboardResponse(
                pending,
                assigned,
                inProgress,
                completed,
                availableVolunteers.size(),
                activeRequests,
                availableVolunteers
        );
    }

    /**
     * List all verified volunteers available for assignment per REQ-COOR-2.
     */
    @Transactional(readOnly = true)
    public List<VolunteerSummaryResponse> getAvailableVolunteers(UserPrincipal principal) {
        if (principal.getRole() == Role.NGO) {
            assistanceService.validateNgoVerified(principal.getUserId());
        } else if (principal.getRole() != Role.ADMIN && principal.getRole() != Role.AUTHORITY) {
            throw new AccessDeniedException("Access denied: only NGOs, Authorities, or Admins may view available volunteers");
        }

        List<VolunteerProfile> verifiedVolunteers = volunteerProfileRepository.findAll().stream()
                .filter(v -> v.getVerificationStatus() == VerificationStatus.VERIFIED)
                .collect(Collectors.toList());

        List<VolunteerSummaryResponse> result = new ArrayList<>();
        for (VolunteerProfile vp : verifiedVolunteers) {
            User user = vp.getUser();
            long activeTasks = assistanceRepository.countByAssignedToAndStatus(user, RequestStatus.ASSIGNED)
                    + assistanceRepository.countByAssignedToAndStatus(user, RequestStatus.ACCEPTED)
                    + assistanceRepository.countByAssignedToAndStatus(user, RequestStatus.IN_PROGRESS);

            result.add(new VolunteerSummaryResponse(
                    user.getUserId(),
                    vp.getVolunteerId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhone(),
                    vp.getSkills(),
                    vp.getAvailability(),
                    vp.getVerificationStatus(),
                    activeTasks
            ));
        }

        return result;
    }
}
