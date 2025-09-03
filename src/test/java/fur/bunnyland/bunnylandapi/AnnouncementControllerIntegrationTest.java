package fur.bunnyland.bunnylandapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import fur.bunnyland.bunnylandapi.api.dto.announce.CreateAnnouncementRequest;
import fur.bunnyland.bunnylandapi.domain.Announcement;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
}

