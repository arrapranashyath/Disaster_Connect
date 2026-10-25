package com.disasterconnect.service;

import com.disasterconnect.dto.MissingPersonReportRequest;
import com.disasterconnect.dto.MissingPersonResponse;
import com.disasterconnect.dto.MissingPersonStatusUpdateRequest;
import com.disasterconnect.entity.MissingPerson;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.enums.MissingPersonStatus;
import com.disasterconnect.entity.enums.Role;
import com.disasterconnect.exception.ResourceNotFoundException;
import com.disasterconnect.repository.MissingPersonRepository;
import com.disasterconnect.repository.UserRepository;
import com.disasterconnect.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MissingPersonService {

    private final MissingPersonRepository missingPersonRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public MissingPersonService(MissingPersonRepository missingPersonRepository,
                                UserRepository userRepository,
                                NotificationService notificationService) {
        this.missingPersonRepository = missingPersonRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public MissingPersonResponse reportMissingPerson(UserPrincipal principal, MissingPersonReportRequest request) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getUserId()));

        MissingPerson missingPerson = new MissingPerson();
        missingPerson.setReportedBy(user);
        missingPerson.setName(request.getName().trim());
        missingPerson.setAge(request.getAge());
        missingPerson.setGender(request.getGender().trim());
        missingPerson.setLastSeenLocation(request.getLastSeenLocation().trim());
        missingPerson.setDescription(request.getDescription().trim());
        missingPerson.setContactInfo(request.getContactInfo().trim());
        missingPerson.setPhotoUrl(request.getPhotoUrl() != null ? request.getPhotoUrl().trim() : null);
        missingPerson.setStatus(MissingPersonStatus.MISSING);

        MissingPerson saved = missingPersonRepository.save(missingPerson);
        return MissingPersonResponse.fromEntity(saved, false);
    }

    @Transactional(readOnly = true)
    public Page<MissingPersonResponse> searchMissingPersons(String query,
                                                            MissingPersonStatus status,
                                                            boolean isAuthenticated,
                                                            Pageable pageable) {
        String searchQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        Page<MissingPerson> page = missingPersonRepository.searchMissingPersons(searchQuery, status, pageable);
        return page.map(mp -> MissingPersonResponse.fromEntity(mp, !isAuthenticated));
    }

    @Transactional(readOnly = true)
    public MissingPersonResponse getMissingPersonById(Long id, boolean isAuthenticated) {
        MissingPerson missingPerson = missingPersonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Missing person record not found with id: " + id));
        return MissingPersonResponse.fromEntity(missingPerson, !isAuthenticated);
    }

    @Transactional
    public MissingPersonResponse updateStatus(Long id,
                                             MissingPersonStatusUpdateRequest request,
                                             UserPrincipal principal) {
        MissingPerson missingPerson = missingPersonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Missing person record not found with id: " + id));

        boolean isReporter = missingPerson.getReportedBy().getUserId().equals(principal.getUserId());
        boolean isAuthority = principal.getRole() == Role.AUTHORITY;
        boolean isAdmin = principal.getRole() == Role.ADMIN;

        if (!isReporter && !isAuthority && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to update this missing person record");
        }

        missingPerson.setStatus(request.getStatus());
        MissingPerson updated = missingPersonRepository.save(missingPerson);

        notificationService.notifyMissingPersonStatusChange(
                updated.getReportedBy(),
                "Missing Person Status Update",
                "Status for " + updated.getName() + " was updated to " + updated.getStatus(),
                updated.getMissingId()
        );

        return MissingPersonResponse.fromEntity(updated, false);
    }
}
