package com.disasterconnect.controller;

import com.disasterconnect.dto.*;
import com.disasterconnect.entity.MissingPerson;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.enums.MissingPersonStatus;
import com.disasterconnect.entity.enums.Role;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MissingPersonControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminActionLogRepository adminActionLogRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ReliefCampaignRepository reliefCampaignRepository;

    @Autowired
    private MissingPersonRepository missingPersonRepository;

    @Autowired
    private ResourceUpdateLogRepository resourceUpdateLogRepository;

    @Autowired
    private ReliefResourceRepository reliefResourceRepository;

    @Autowired
    private ShelterRepository shelterRepository;

    @Autowired
    private VolunteerProfileRepository volunteerProfileRepository;

    @Autowired
    private NGOProfileRepository ngoProfileRepository;

    @Autowired
    private RequestStatusLogRepository requestStatusLogRepository;

    @Autowired
    private AssistanceRequestRepository assistanceRequestRepository;

    @Autowired
    private IncidentReportRepository incidentReportRepository;

    @Autowired
    private DisasterAlertRepository disasterAlertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User citizen1;
    private User citizen2;
    private User volunteerUser;
    private User authorityUser;
    private User adminUser;

    private String citizen1Token;
    private String citizen2Token;
    private String volunteerToken;
    private String authorityToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        cleanupDatabase();

        citizen1 = userRepository.save(new User("Citizen One", "mp_cit1@example.com", passwordEncoder.encode("Password123!"), "+911111111111", Role.CITIZEN, "Pune"));
        citizen1Token = login("mp_cit1@example.com", "Password123!");

        citizen2 = userRepository.save(new User("Citizen Two", "mp_cit2@example.com", passwordEncoder.encode("Password123!"), "+912222222222", Role.CITIZEN, "Pune"));
        citizen2Token = login("mp_cit2@example.com", "Password123!");

        volunteerUser = userRepository.save(new User("Volunteer User", "mp_vol@example.com", passwordEncoder.encode("Password123!"), "+913333333333", Role.VOLUNTEER, "Pune"));
        volunteerToken = login("mp_vol@example.com", "Password123!");

        authorityUser = userRepository.save(new User("Authority User", "mp_auth@example.com", passwordEncoder.encode("Password123!"), "+914444444444", Role.AUTHORITY, "Pune"));
        authorityToken = login("mp_auth@example.com", "Password123!");

        adminUser = userRepository.save(new User("Admin User", "mp_admin@example.com", passwordEncoder.encode("Password123!"), "+915555555555", Role.ADMIN, "Pune"));
        adminToken = login("mp_admin@example.com", "Password123!");
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        cleanupDatabase();
    }

    private void cleanupDatabase() {
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

    private String login(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<AuthResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );
        return response.getData().getToken();
    }

    @Test
    @DisplayName("REQ-MISS-1: Citizen reports missing person successfully")
    void testReportMissingPerson_Citizen_Success() throws Exception {
        MissingPersonReportRequest request = new MissingPersonReportRequest(
                "Ramesh Sharma", 45, "Male", "Shivaji Nagar Bus Stand",
                "Wearing blue shirt and black pants, scar on forehead",
                "+919876543210", "https://example.com/ramesh.jpg"
        );

        mockMvc.perform(post("/api/missing-persons")
                        .header("Authorization", "Bearer " + citizen1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Ramesh Sharma"))
                .andExpect(jsonPath("$.data.age").value(45))
                .andExpect(jsonPath("$.data.gender").value("Male"))
                .andExpect(jsonPath("$.data.status").value("MISSING"))
                .andExpect(jsonPath("$.data.contactInfo").value("+919876543210"))
                .andExpect(jsonPath("$.data.reportedByUserId").value(citizen1.getUserId()));

        assertEquals(1, missingPersonRepository.count());
    }

    @Test
    @DisplayName("REQ-MISS-1: Volunteer reports missing person successfully")
    void testReportMissingPerson_Volunteer_Success() throws Exception {
        MissingPersonReportRequest request = new MissingPersonReportRequest(
                "Geeta Devi", 60, "Female", "Kothrud Depot",
                "Elderly lady with spectacles, red saree",
                "+919123456780", null
        );

        mockMvc.perform(post("/api/missing-persons")
                        .header("Authorization", "Bearer " + volunteerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Geeta Devi"))
                .andExpect(jsonPath("$.data.status").value("MISSING"));
    }

    @Test
    @DisplayName("REQ-MISS-1: Validation failure on missing required fields")
    void testReportMissingPerson_ValidationFailure() throws Exception {
        MissingPersonReportRequest request = new MissingPersonReportRequest(
                "", null, "", "", "", "", null
        );

        mockMvc.perform(post("/api/missing-persons")
                        .header("Authorization", "Bearer " + citizen1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("REQ-MISS-1: Unauthenticated request to report missing person is rejected")
    void testReportMissingPerson_Unauthenticated() throws Exception {
        MissingPersonReportRequest request = new MissingPersonReportRequest(
                "Anil Roy", 30, "Male", "Camp", "Tall build", "+919999999999", null
        );

        mockMvc.perform(post("/api/missing-persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("REQ-MISS-4: Unauthenticated search masks contact details")
    void testSearchMissingPersons_Unauthenticated_ContactMasked() throws Exception {
        MissingPerson mp = new MissingPerson(
                citizen1, "Suresh Patil", 52, "Male", "Swargate",
                "Last seen near metro station", "+919876500000", null, MissingPersonStatus.MISSING
        );
        missingPersonRepository.save(mp);

        mockMvc.perform(get("/api/missing-persons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Suresh Patil"))
                .andExpect(jsonPath("$.data.content[0].contactInfo").value("[PROTECTED - LOGIN TO VIEW]"));
    }

    @Test
    @DisplayName("REQ-MISS-2 & REQ-MISS-4: Authenticated user sees unmasked contact details")
    void testSearchMissingPersons_Authenticated_ContactUnmasked() throws Exception {
        MissingPerson mp = new MissingPerson(
                citizen1, "Suresh Patil", 52, "Male", "Swargate",
                "Last seen near metro station", "+919876500000", null, MissingPersonStatus.MISSING
        );
        missingPersonRepository.save(mp);

        mockMvc.perform(get("/api/missing-persons")
                        .header("Authorization", "Bearer " + citizen2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Suresh Patil"))
                .andExpect(jsonPath("$.data.content[0].contactInfo").value("+919876500000"));
    }

    @Test
    @DisplayName("REQ-MISS-2: Search filters by name and location query")
    void testSearchMissingPersons_FilterByQuery() throws Exception {
        MissingPerson mp1 = new MissingPerson(
                citizen1, "Aarav Kumar", 10, "Male", "Aundh Bridge",
                "Blue school bag", "+911111111111", null, MissingPersonStatus.MISSING
        );
        MissingPerson mp2 = new MissingPerson(
                citizen1, "Sunita Rao", 35, "Female", "Hadapsar",
                "Green dress", "+912222222222", null, MissingPersonStatus.MISSING
        );
        missingPersonRepository.save(mp1);
        missingPersonRepository.save(mp2);

        mockMvc.perform(get("/api/missing-persons")
                        .param("query", "Aarav"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Aarav Kumar"));

        mockMvc.perform(get("/api/missing-persons")
                        .param("query", "Hadapsar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Sunita Rao"));
    }

    @Test
    @DisplayName("REQ-MISS-3: Original reporter can update status to FOUND")
    void testUpdateStatus_ByReporter_Success() throws Exception {
        MissingPerson mp = missingPersonRepository.save(new MissingPerson(
                citizen1, "Pooja Hegde", 28, "Female", "Baner",
                "Brown hair", "+913333333333", null, MissingPersonStatus.MISSING
        ));

        MissingPersonStatusUpdateRequest updateReq = new MissingPersonStatusUpdateRequest(MissingPersonStatus.FOUND);

        mockMvc.perform(patch("/api/missing-persons/" + mp.getMissingId() + "/status")
                        .header("Authorization", "Bearer " + citizen1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("FOUND"));

        MissingPerson updated = missingPersonRepository.findById(mp.getMissingId()).orElseThrow();
        assertEquals(MissingPersonStatus.FOUND, updated.getStatus());
    }

    @Test
    @DisplayName("REQ-MISS-3: Authority can update status to CLOSED")
    void testUpdateStatus_ByAuthority_Success() throws Exception {
        MissingPerson mp = missingPersonRepository.save(new MissingPerson(
                citizen1, "Pooja Hegde", 28, "Female", "Baner",
                "Brown hair", "+913333333333", null, MissingPersonStatus.MISSING
        ));

        MissingPersonStatusUpdateRequest updateReq = new MissingPersonStatusUpdateRequest(MissingPersonStatus.CLOSED);

        mockMvc.perform(patch("/api/missing-persons/" + mp.getMissingId() + "/status")
                        .header("Authorization", "Bearer " + authorityToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }

    @Test
    @DisplayName("REQ-MISS-3: Non-reporter Citizen cannot update status (Forbidden)")
    void testUpdateStatus_ByNonReporterCitizen_Forbidden() throws Exception {
        MissingPerson mp = missingPersonRepository.save(new MissingPerson(
                citizen1, "Pooja Hegde", 28, "Female", "Baner",
                "Brown hair", "+913333333333", null, MissingPersonStatus.MISSING
        ));

        MissingPersonStatusUpdateRequest updateReq = new MissingPersonStatusUpdateRequest(MissingPersonStatus.FOUND);

        mockMvc.perform(patch("/api/missing-persons/" + mp.getMissingId() + "/status")
                        .header("Authorization", "Bearer " + citizen2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("REQ-MISS-3: Updating non-existent missing person returns 404")
    void testUpdateStatus_NotFound() throws Exception {
        MissingPersonStatusUpdateRequest updateReq = new MissingPersonStatusUpdateRequest(MissingPersonStatus.FOUND);

        mockMvc.perform(patch("/api/missing-persons/999999/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());
    }
}
