package com.disasterconnect.controller;

import com.disasterconnect.dto.*;
import com.disasterconnect.entity.Shelter;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.enums.Role;
import com.disasterconnect.entity.enums.ServiceType;
import com.disasterconnect.entity.enums.ShelterStatus;
import com.disasterconnect.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ShelterControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShelterRepository shelterRepository;

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
    private RequestStatusLogRepository requestStatusLogRepository;

    @Autowired
    private AssistanceRequestRepository assistanceRequestRepository;

    @Autowired
    private IncidentReportRepository incidentReportRepository;

    @Autowired
    private DisasterAlertRepository disasterAlertRepository;

    @Autowired
    private com.disasterconnect.repository.AdminActionLogRepository adminActionLogRepository;

    @Autowired
    private com.disasterconnect.repository.NotificationRepository notificationRepository;

    @Autowired
    private com.disasterconnect.repository.ReliefCampaignRepository reliefCampaignRepository;

    @Autowired
    private com.disasterconnect.repository.MissingPersonRepository missingPersonRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User authorityUser;
    private User citizenUser;
    private String adminToken;
    private String authorityToken;
    private String citizenToken;

    @BeforeEach
    void setUp() throws Exception {
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

        // Admin
        adminUser = new User("Shelter Admin", "sh_admin@example.com", passwordEncoder.encode("Password123!"), "+911111111111", Role.ADMIN, "Pune");
        adminUser = userRepository.save(adminUser);
        adminToken = login("sh_admin@example.com", "Password123!");

        // Authority
        authorityUser = new User("Shelter Authority", "sh_authority@example.com", passwordEncoder.encode("Password123!"), "+912222222222", Role.AUTHORITY, "Pune");
        authorityUser = userRepository.save(authorityUser);
        authorityToken = login("sh_authority@example.com", "Password123!");

        // Citizen
        citizenUser = new User("Shelter Citizen", "sh_citizen@example.com", passwordEncoder.encode("Password123!"), "+913333333333", Role.CITIZEN, "Pune");
        citizenUser = userRepository.save(citizenUser);
        citizenToken = login("sh_citizen@example.com", "Password123!");
    }

    private String login(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest(email, password);
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    @Test
    @DisplayName("Authority should successfully create a shelter (201 Created)")
    void testCreateShelterSuccess() throws Exception {
        ShelterCreateRequest request = new ShelterCreateRequest();
        request.setName("Central Relief Camp");
        request.setServiceType(ServiceType.SHELTER);
        request.setAddress("Shivajinagar Ground, Pune");
        request.setCity("Pune");
        request.setLatitude(18.5314);
        request.setLongitude(73.8446);
        request.setCapacity(500);
        request.setAvailableSpace(350);
        request.setContactPhone("+919876500000");
        request.setContactEmail("camp@pune.gov.in");

        MvcResult result = mockMvc.perform(post("/api/shelters")
                        .header("Authorization", "Bearer " + authorityToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Central Relief Camp"))
                .andExpect(jsonPath("$.data.serviceType").value("SHELTER"))
                .andExpect(jsonPath("$.data.availableSpace").value(350))
                .andReturn();

        ApiResponse<ShelterResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertNotNull(response.getData().getShelterId());
        assertEquals(authorityUser.getUserId(), response.getData().getCreatedById());
    }

    @Test
    @DisplayName("Admin should successfully create an essential service (Hospital)")
    void testCreateEssentialServiceSuccess() throws Exception {
        ShelterCreateRequest request = new ShelterCreateRequest();
        request.setName("General Emergency Hospital");
        request.setServiceType(ServiceType.HOSPITAL);
        request.setAddress("Station Road, Pune");
        request.setCity("Pune");
        request.setLatitude(18.5284);
        request.setLongitude(73.8743);
        request.setContactPhone("+919876511111");

        mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.serviceType").value("HOSPITAL"));
    }

    @Test
    @DisplayName("Citizen should be forbidden (403) from creating a shelter")
    void testCitizenForbiddenFromCreatingShelter() throws Exception {
        ShelterCreateRequest request = new ShelterCreateRequest();
        request.setName("Unauthorized Shelter");
        request.setServiceType(ServiceType.SHELTER);
        request.setAddress("Street 1");
        request.setLatitude(18.50);
        request.setLongitude(73.80);

        mockMvc.perform(post("/api/shelters")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated request to create shelter should return 401 Unauthorized")
    void testUnauthenticatedCreateShelter() throws Exception {
        ShelterCreateRequest request = new ShelterCreateRequest();
        request.setName("No Auth Shelter");
        request.setServiceType(ServiceType.SHELTER);
        request.setAddress("Street 2");
        request.setLatitude(18.50);
        request.setLongitude(73.80);

        mockMvc.perform(post("/api/shelters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Validation failure: missing required fields should return 400 Bad Request")
    void testCreateShelterValidationFailure() throws Exception {
        ShelterCreateRequest invalidRequest = new ShelterCreateRequest();

        mockMvc.perform(post("/api/shelters")
                        .header("Authorization", "Bearer " + authorityToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Validation failure: availableSpace greater than capacity should return 400 Bad Request")
    void testAvailableSpaceExceedsCapacity() throws Exception {
        ShelterCreateRequest request = new ShelterCreateRequest();
        request.setName("Overflow Shelter");
        request.setServiceType(ServiceType.SHELTER);
        request.setAddress("Main Gate");
        request.setLatitude(18.50);
        request.setLongitude(73.80);
        request.setCapacity(100);
        request.setAvailableSpace(150);

        mockMvc.perform(post("/api/shelters")
                        .header("Authorization", "Bearer " + authorityToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Public directory: GET /api/shelters should allow unauthenticated access and filter by type")
    void testPublicGetSheltersWithFilter() throws Exception {
        Shelter s1 = new Shelter("Camp Alpha", ServiceType.SHELTER, "Camp Area", "Pune", 18.52, 73.85, 200, 100, null, null, ShelterStatus.ACTIVE, adminUser);
        Shelter s2 = new Shelter("City Hospital", ServiceType.HOSPITAL, "Camp Area", "Pune", 18.53, 73.86, null, null, null, null, ShelterStatus.ACTIVE, adminUser);
        shelterRepository.saveAll(List.of(s1, s2));

        mockMvc.perform(get("/api/shelters")
                        .param("type", "SHELTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Camp Alpha"));

        mockMvc.perform(get("/api/shelters")
                        .param("type", "HOSPITAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("City Hospital"));
    }

    @Test
    @DisplayName("Proximity search: GET /api/shelters/nearby should calculate distance and sort ascending")
    void testNearbySheltersSortedByProximity() throws Exception {
        Shelter near = new Shelter("Near Relief Center", ServiceType.RELIEF_CENTER, "Camp", "Pune", 18.5250, 73.8650, 100, 50, null, null, ShelterStatus.ACTIVE, adminUser);
        Shelter far = new Shelter("Far Relief Center", ServiceType.RELIEF_CENTER, "Wakad", "Pune", 18.5987, 73.7684, 100, 80, null, null, ShelterStatus.ACTIVE, adminUser);
        shelterRepository.saveAll(List.of(far, near));

        MvcResult result = mockMvc.perform(get("/api/shelters/nearby")
                        .param("latitude", "18.5204")
                        .param("longitude", "73.8567")
                        .param("type", "RELIEF_CENTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        ApiResponse<List<ShelterResponse>> response = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});

        List<ShelterResponse> list = response.getData();
        assertEquals(2, list.size());
        assertEquals("Near Relief Center", list.get(0).getName());
        assertEquals("Far Relief Center", list.get(1).getName());
        assertNotNull(list.get(0).getDistanceKm());
        assertTrue(list.get(0).getDistanceKm() < list.get(1).getDistanceKm());
    }

    @Test
    @DisplayName("Authority updates shelter capacity and status (200 OK)")
    void testUpdateShelterSuccess() throws Exception {
        Shelter shelter = new Shelter("Update Camp", ServiceType.SHELTER, "Address 1", "Pune", 18.52, 73.85, 300, 200, null, null, ShelterStatus.ACTIVE, authorityUser);
        shelter = shelterRepository.save(shelter);

        ShelterUpdateRequest updateRequest = new ShelterUpdateRequest();
        updateRequest.setAvailableSpace(50);
        updateRequest.setStatus(ShelterStatus.FULL);

        mockMvc.perform(patch("/api/shelters/" + shelter.getShelterId())
                        .header("Authorization", "Bearer " + authorityToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.availableSpace").value(50))
                .andExpect(jsonPath("$.data.status").value("FULL"));
    }

    @Test
    @DisplayName("Authority deactivates shelter (200 OK)")
    void testDeactivateShelterSuccess() throws Exception {
        Shelter shelter = new Shelter("Close Camp", ServiceType.SHELTER, "Address 2", "Pune", 18.52, 73.85, 100, 50, null, null, ShelterStatus.ACTIVE, authorityUser);
        shelter = shelterRepository.save(shelter);

        mockMvc.perform(patch("/api/shelters/" + shelter.getShelterId() + "/deactivate")
                        .header("Authorization", "Bearer " + authorityToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        Shelter found = shelterRepository.findById(shelter.getShelterId()).orElseThrow();
        assertEquals(ShelterStatus.INACTIVE, found.getStatus());
    }
}
