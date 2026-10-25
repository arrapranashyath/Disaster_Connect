package com.disasterconnect.controller;

import com.disasterconnect.dto.*;
import com.disasterconnect.entity.DisasterAlert;
import com.disasterconnect.entity.MissingPerson;
import com.disasterconnect.entity.Notification;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.enums.AlertSeverity;
import com.disasterconnect.entity.enums.AlertStatus;
import com.disasterconnect.entity.enums.MissingPersonStatus;
import com.disasterconnect.entity.enums.NotificationType;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminActionLogRepository adminActionLogRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ReliefCampaignRepository campaignRepository;

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
    private User adminUser;

    private String citizen1Token;
    private String citizen2Token;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        cleanupDatabase();

        citizen1 = userRepository.save(new User("Citizen One", "notif_cit1@example.com", passwordEncoder.encode("Password123!"), "+911111111111", Role.CITIZEN, "Pune"));
        citizen1Token = login("notif_cit1@example.com", "Password123!");

        citizen2 = userRepository.save(new User("Citizen Two", "notif_cit2@example.com", passwordEncoder.encode("Password123!"), "+912222222222", Role.CITIZEN, "Mumbai"));
        citizen2Token = login("notif_cit2@example.com", "Password123!");

        adminUser = userRepository.save(new User("Admin User", "notif_adm@example.com", passwordEncoder.encode("Password123!"), "+913333333333", Role.ADMIN, "Pune"));
        adminToken = login("notif_adm@example.com", "Password123!");
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        cleanupDatabase();
    }

    private void cleanupDatabase() {
        adminActionLogRepository.deleteAll();
        notificationRepository.deleteAll();
        campaignRepository.deleteAll();
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
    @DisplayName("REQ-NOTIF-1: Creating alert triggers notifications for affected area users")
    void testAlertCreationTriggersNotification() throws Exception {
        AlertCreateRequest alertRequest = new AlertCreateRequest(
                "Flood", "Severe Flood Warning Pune", "Water levels rising in Mutha river",
                "Pune", AlertSeverity.HIGH, "Evacuate low lying areas"
        );

        mockMvc.perform(post("/api/alerts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(alertRequest)))
                .andExpect(status().isCreated());

        // citizen1 is in Pune -> should have received notification
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + citizen1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].type").value("ALERT"))
                .andExpect(jsonPath("$.data.content[0].title").value("Disaster Alert: Severe Flood Warning Pune"));
    }

    @Test
    @DisplayName("REQ-NOTIF-3: Missing person status change triggers notification to reporter")
    void testMissingPersonStatusChangeTriggersNotification() throws Exception {
        MissingPerson mp = missingPersonRepository.save(new MissingPerson(
                citizen1, "Vikas Gupta", 32, "Male", "Shivaji Nagar",
                "Blue shirt", "+919999999999", null, MissingPersonStatus.MISSING
        ));

        MissingPersonStatusUpdateRequest updateReq = new MissingPersonStatusUpdateRequest(MissingPersonStatus.FOUND);

        mockMvc.perform(patch("/api/missing-persons/" + mp.getMissingId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk());

        // citizen1 is the reporter -> should have received notification
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + citizen1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].type").value("MISSING_PERSON"))
                .andExpect(jsonPath("$.data.content[0].isRead").value(false));
    }

    @Test
    @DisplayName("REQ-NOTIF-4: Get unread count and filter unread notifications")
    void testGetUnreadNotificationsAndCount() throws Exception {
        Notification n1 = new Notification(citizen1, NotificationType.SYSTEM, "Welcome", "Welcome to DisasterConnect", null, null);
        Notification n2 = new Notification(citizen1, NotificationType.ALERT, "Test Alert", "Alert info", null, null);
        n2.setRead(true);

        notificationRepository.save(n1);
        notificationRepository.save(n2);

        // Check unread count
        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + citizen1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(1));

        // Filter unread only
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + citizen1Token)
                        .param("unreadOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].notificationId").value(n1.getNotificationId()));
    }

    @Test
    @DisplayName("REQ-NOTIF-4: Mark single notification as read")
    void testMarkAsRead_Success() throws Exception {
        Notification n = notificationRepository.save(new Notification(
                citizen1, NotificationType.SYSTEM, "Test", "Message", null, null
        ));

        mockMvc.perform(patch("/api/notifications/" + n.getNotificationId() + "/read")
                        .header("Authorization", "Bearer " + citizen1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isRead").value(true));

        Notification updated = notificationRepository.findById(n.getNotificationId()).orElseThrow();
        assertTrue(updated.isRead());
    }

    @Test
    @DisplayName("REQ-NOTIF-4: Other user cannot mark notification as read (Forbidden)")
    void testMarkAsRead_OtherUser_Forbidden() throws Exception {
        Notification n = notificationRepository.save(new Notification(
                citizen1, NotificationType.SYSTEM, "Test", "Message", null, null
        ));

        mockMvc.perform(patch("/api/notifications/" + n.getNotificationId() + "/read")
                        .header("Authorization", "Bearer " + citizen2Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("REQ-NOTIF-4: Mark all notifications as read")
    void testMarkAllAsRead_Success() throws Exception {
        Notification n1 = new Notification(citizen1, NotificationType.SYSTEM, "One", "Msg 1", null, null);
        Notification n2 = new Notification(citizen1, NotificationType.SYSTEM, "Two", "Msg 2", null, null);
        notificationRepository.save(n1);
        notificationRepository.save(n2);

        mockMvc.perform(patch("/api/notifications/read-all")
                        .header("Authorization", "Bearer " + citizen1Token))
                .andExpect(status().isOk());

        assertEquals(0, notificationRepository.countByUserAndIsReadFalse(citizen1));
    }
}
