package fur.bunnyland.bunnylandapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import fur.bunnyland.bunnylandapi.api.dto.announce.CreateAnnouncementRequest;
import fur.bunnyland.bunnylandapi.domain.Announcement;
import fur.bunnyland.bunnylandapi.domain.AnnouncementStatus;
import fur.bunnyland.bunnylandapi.domain.User;
import fur.bunnyland.bunnylandapi.repository.AnnouncementRepository;
import fur.bunnyland.bunnylandapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AnnouncementControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createPersistsAnnouncement() throws Exception {
        announcementRepository.deleteAll();
        userRepository.deleteAll();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"owner@example.com\",\"password\":\"pw\",\"displayName\":\"Owner\"}"))
                .andExpect(status().isCreated());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"owner@example.com\",\"password\":\"pw\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(login.getResponse().getContentAsString())
                .get("body").get("body").get("accessToken").asText();

        CreateAnnouncementRequest req = new CreateAnnouncementRequest(
                "title", "desc", "city", "country",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2));

        mockMvc.perform(post("/api/announcements")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("title"));

        assertThat(announcementRepository.count()).isEqualTo(1);
        Announcement saved = announcementRepository.findAll().get(0);
        assertThat(saved.getTitle()).isEqualTo("title");
        assertThat(saved.getOwner().getEmail()).isEqualTo("owner@example.com");
    }

    @Test
    void createReturnsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(post("/api/announcements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"t\",\"description\":\"d\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listReturnsAllAnnouncements() throws Exception {
        announcementRepository.deleteAll();
        userRepository.deleteAll();

        User owner = new User();
        owner.setEmail("owner@example.com");
        owner.setPasswordHash("pw");
        owner.setDisplayName("Owner");
        owner = userRepository.save(owner);

        Announcement a = new Announcement();
        a.setOwner(owner);
        a.setTitle("title1");
        a.setDescription("desc1");
        announcementRepository.save(a);

        mockMvc.perform(get("/api/announcements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("title1"))
                .andExpect(jsonPath("$[0].ownerId").value(owner.getId()));
    }

    @Test
    void getReturnsAnnouncementById() throws Exception {
        announcementRepository.deleteAll();
        userRepository.deleteAll();

        User owner = new User();
        owner.setEmail("owner@example.com");
        owner.setPasswordHash("pw");
        owner.setDisplayName("Owner");
        owner = userRepository.save(owner);

        Announcement a = new Announcement();
        a.setOwner(owner);
        a.setTitle("title1");
        a.setDescription("desc1");
        a = announcementRepository.save(a);

        mockMvc.perform(get("/api/announcements/" + a.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(a.getId()))
                .andExpect(jsonPath("$.title").value("title1"))
                .andExpect(jsonPath("$.ownerId").value(owner.getId()));
    }

    @Test
    void getReturnsNotFoundForMissingAnnouncement() throws Exception {
        mockMvc.perform(get("/api/announcements/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAllowsOwnerToRemoveAnnouncement() throws Exception {
        announcementRepository.deleteAll();
        userRepository.deleteAll();

        // Register and login owner
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"owner1@example.com\",\"password\":\"pw\",\"displayName\":\"Owner1\"}"))
                .andExpect(status().isCreated());
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"owner1@example.com\",\"password\":\"pw\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String token = objectMapper.readTree(login.getResponse().getContentAsString())
                .get("body").get("body").get("accessToken").asText();

        // Create announcement
        CreateAnnouncementRequest req = new CreateAnnouncementRequest(
                "t", "d", null, null, null, null);
        mockMvc.perform(post("/api/announcements")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
        Announcement a = announcementRepository.findAll().get(0);

        mockMvc.perform(delete("/api/announcements/" + a.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(a.getId()))
                .andExpect(jsonPath("$.status").value(AnnouncementStatus.DELETED.name()));

        Announcement deleted = announcementRepository.findById(a.getId()).orElseThrow();
        assertThat(deleted.getStatus()).isEqualTo(AnnouncementStatus.DELETED.name());
    }

    @Test
    void deletePreventsNonOwner() throws Exception {
        announcementRepository.deleteAll();
        userRepository.deleteAll();

        // Owner 1
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"owner1@example.com\",\"password\":\"pw\",\"displayName\":\"Owner1\"}"))
                .andExpect(status().isCreated());
        MvcResult login1 = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"owner1@example.com\",\"password\":\"pw\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String token1 = objectMapper.readTree(login1.getResponse().getContentAsString())
                .get("body").get("body").get("accessToken").asText();
        // Create announcement by owner1
        CreateAnnouncementRequest req = new CreateAnnouncementRequest(
                "t", "d", null, null, null, null);
        mockMvc.perform(post("/api/announcements")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
        Announcement a = announcementRepository.findAll().get(0);

        // Owner 2
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"owner2@example.com\",\"password\":\"pw\",\"displayName\":\"Owner2\"}"))
                .andExpect(status().isCreated());
        MvcResult login2 = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"owner2@example.com\",\"password\":\"pw\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String token2 = objectMapper.readTree(login2.getResponse().getContentAsString())
                .get("body").get("body").get("accessToken").asText();

        mockMvc.perform(delete("/api/announcements/" + a.getId())
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isForbidden());

        Announcement still = announcementRepository.findById(a.getId()).orElseThrow();
        assertThat(still.getStatus()).isEqualTo(AnnouncementStatus.OPEN.name());
    }

    @Test
    void deleteAllowsAdminToRemoveAnyAnnouncement() throws Exception {
        announcementRepository.deleteAll();
        userRepository.deleteAll();

        // Owner creates announcement
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"owner1@example.com\",\"password\":\"pw\",\"displayName\":\"Owner1\"}"))
                .andExpect(status().isCreated());
        MvcResult ownerLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"owner1@example.com\",\"password\":\"pw\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String ownerToken = objectMapper.readTree(ownerLogin.getResponse().getContentAsString())
                .get("body").get("body").get("accessToken").asText();
        CreateAnnouncementRequest req = new CreateAnnouncementRequest(
                "t", "d", null, null, null, null);
        mockMvc.perform(post("/api/announcements")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
        Announcement a = announcementRepository.findAll().get(0);

        // Create admin user
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@example.com\",\"password\":\"pw\",\"displayName\":\"Admin\"}"))
                .andExpect(status().isCreated());
        User admin = userRepository.findByEmailIgnoreCase("admin@example.com").orElseThrow();
        admin.setRoles(Set.of("ADMIN"));
        userRepository.save(admin);
        MvcResult adminLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@example.com\",\"password\":\"pw\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String adminToken = objectMapper.readTree(adminLogin.getResponse().getContentAsString())
                .get("body").get("body").get("accessToken").asText();

        mockMvc.perform(delete("/api/announcements/" + a.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(a.getId()))
                .andExpect(jsonPath("$.status").value(AnnouncementStatus.DELETED.name()));

        Announcement deleted = announcementRepository.findById(a.getId()).orElseThrow();
        assertThat(deleted.getStatus()).isEqualTo(AnnouncementStatus.DELETED.name());
    }
}

