package com.disasterconnect.service;

import com.disasterconnect.dto.AuthResponse;
import com.disasterconnect.dto.RegisterRequest;
import com.disasterconnect.entity.NGOProfile;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.VolunteerProfile;
import com.disasterconnect.entity.enums.Role;
import com.disasterconnect.entity.enums.VerificationStatus;
import com.disasterconnect.exception.BadRequestException;
import com.disasterconnect.exception.DuplicateEmailException;
import com.disasterconnect.repository.DisasterAlertRepository;
import com.disasterconnect.repository.IncidentReportRepository;
import com.disasterconnect.repository.NGOProfileRepository;
import com.disasterconnect.repository.UserRepository;
import com.disasterconnect.repository.VolunteerProfileRepository;
import com.disasterconnect.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private com.disasterconnect.repository.AdminActionLogRepository adminActionLogRepository;

    @Autowired
    private com.disasterconnect.repository.NotificationRepository notificationRepository;

    @Autowired
    private com.disasterconnect.repository.ReliefCampaignRepository reliefCampaignRepository;

    @Autowired
    private com.disasterconnect.repository.MissingPersonRepository missingPersonRepository;

    @Autowired
    private com.disasterconnect.repository.ShelterRepository shelterRepository;

    @Autowired
    private com.disasterconnect.repository.ResourceUpdateLogRepository resourceUpdateLogRepository;

    @Autowired
    private com.disasterconnect.repository.ReliefResourceRepository reliefResourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NGOProfileRepository ngoProfileRepository;

    @Autowired
    private VolunteerProfileRepository volunteerProfileRepository;

    @Autowired
    private IncidentReportRepository incidentReportRepository;

    @Autowired
    private DisasterAlertRepository disasterAlertRepository;

    @Autowired
    private com.disasterconnect.repository.AssistanceRequestRepository assistanceRequestRepository;

    @Autowired
    private com.disasterconnect.repository.RequestStatusLogRepository requestStatusLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        adminActionLogRepository.deleteAll();
        notificationRepository.deleteAll();
        reliefCampaignRepository.deleteAll();
        missingPersonRepository.deleteAll();
        resourceUpdateLogRepository.deleteAll();
        reliefResourceRepository.deleteAll();
        shelterRepository.deleteAll();
        volunteerProfileRepository.deleteAll();
        ngoProfileRepository.deleteAll();
        requestStatusLogRepository.deleteAll();
        assistanceRequestRepository.deleteAll();
        incidentReportRepository.deleteAll();
        disasterAlertRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should successfully register citizen and hash password using BCrypt")
    void testSuccessfulCitizenRegistrationAndHashing() {
        RegisterRequest request = new RegisterRequest(
                "Jane Doe",
                "jane.doe@example.com",
                "secretPassword123",
                "+919876543210",
                Role.CITIZEN,
                "Pune, Maharashtra"
        );

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("jane.doe@example.com", response.getUser().getEmail());
        assertEquals(Role.CITIZEN, response.getUser().getRole());

        // Verify entity in database
        User savedUser = userRepository.findByEmail("jane.doe@example.com").orElse(null);
        assertNotNull(savedUser);
        assertNotEquals("secretPassword123", savedUser.getPassword());
        assertTrue(passwordEncoder.matches("secretPassword123", savedUser.getPassword()),
                "Stored password hash must match raw password with BCrypt");
    }

    @Test
    @DisplayName("Should register NGO and create NGOProfile with PENDING verification status")
    void testNgoRegistrationCreatesPendingNgoProfile() {
        RegisterRequest request = new RegisterRequest(
                "Relief Org Leader",
                "relief@ngo.org",
                "securePass123",
                "+919876543210",
                Role.NGO,
                "Kerala"
        );
        request.setOrgName("Hope Relief Foundation");
        request.setRegistrationNumber("NGO-REG-2026-99");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(Role.NGO, response.getUser().getRole());
        assertEquals("Hope Relief Foundation", response.getUser().getOrgName());
        assertEquals(VerificationStatus.PENDING, response.getUser().getVerificationStatus());

        User savedUser = userRepository.findByEmail("relief@ngo.org").orElse(null);
        assertNotNull(savedUser);
        NGOProfile profile = ngoProfileRepository.findByUser(savedUser).orElse(null);
        assertNotNull(profile, "NGO profile record must be created");
        assertEquals("Hope Relief Foundation", profile.getOrgName());
        assertEquals("NGO-REG-2026-99", profile.getRegistrationNumber());
        assertEquals(VerificationStatus.PENDING, profile.getVerificationStatus(),
                "Initial NGO status must be PENDING per SRS REQ-USER-4");
    }

    @Test
    @DisplayName("Should register Volunteer and create VolunteerProfile")
    void testVolunteerRegistrationCreatesVolunteerProfile() {
        RegisterRequest request = new RegisterRequest(
                "Volunteer Sam",
                "sam@volunteer.org",
                "securePass123",
                "+919876543210",
                Role.VOLUNTEER,
                "Pune"
        );
        request.setSkills("Emergency First Aid, Driving");
        request.setAvailability("AVAILABLE");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(Role.VOLUNTEER, response.getUser().getRole());
        assertEquals("Emergency First Aid, Driving", response.getUser().getSkills());
        assertEquals("AVAILABLE", response.getUser().getAvailability());
        assertEquals(VerificationStatus.PENDING, response.getUser().getVerificationStatus(),
                "Initial volunteer status in UserResponse must be PENDING");

        User savedUser = userRepository.findByEmail("sam@volunteer.org").orElse(null);
        assertNotNull(savedUser);
        VolunteerProfile profile = volunteerProfileRepository.findByUser(savedUser).orElse(null);
        assertNotNull(profile, "Volunteer profile record must be created");
        assertEquals("Emergency First Aid, Driving", profile.getSkills());
        assertEquals("AVAILABLE", profile.getAvailability());
        assertEquals(VerificationStatus.PENDING, profile.getVerificationStatus(),
                "Initial volunteer status in database must be PENDING");
    }

    @Test
    @DisplayName("Should prevent duplicate registration with the same email")
    void testDuplicateRegistrationFails() {
        RegisterRequest first = new RegisterRequest(
                "First User",
                "duplicate@example.com",
                "password123",
                "+919876543210",
                Role.CITIZEN,
                "Mumbai"
        );
        authService.register(first);

        RegisterRequest second = new RegisterRequest(
                "Second User",
                "duplicate@example.com",
                "differentPassword",
                "+919876543211",
                Role.CITIZEN,
                "Delhi"
        );

        assertThrows(DuplicateEmailException.class, () -> authService.register(second));
    }

    @Test
    @DisplayName("Should reject self-registration as ADMIN or AUTHORITY")
    void testRejectSelfRegistrationAsAdminOrAuthority() {
        RegisterRequest adminReq = new RegisterRequest(
                "Admin User",
                "admin.attempt@example.com",
                "adminPass123",
                "+919876543210",
                Role.ADMIN,
                "Headquarters"
        );

        assertThrows(BadRequestException.class, () -> authService.register(adminReq));

        RegisterRequest authReq = new RegisterRequest(
                "Authority User",
                "authority.attempt@example.com",
                "authPass123",
                "+919876543210",
                Role.AUTHORITY,
                "Police Station"
        );

        assertThrows(BadRequestException.class, () -> authService.register(authReq));
    }

    @Test
    @DisplayName("JwtTokenProvider startup validation must reject missing or insecure secret")
    void testJwtSecretValidationRejectsWeakSecrets() {
        assertThrows(IllegalStateException.class, () -> new JwtTokenProvider(null, 86400000L));
        assertThrows(IllegalStateException.class, () -> new JwtTokenProvider("", 86400000L));
        assertThrows(IllegalStateException.class, () -> new JwtTokenProvider("short-secret-less-than-32-bytes", 86400000L));
    }
}
