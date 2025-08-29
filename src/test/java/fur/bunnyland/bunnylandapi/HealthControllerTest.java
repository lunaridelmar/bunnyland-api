package fur.bunnyland.bunnylandapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import fur.bunnyland.bunnylandapi.config.SecurityConfig;
import fur.bunnyland.bunnylandapi.controller.HealthController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HealthController.class)
@Import(SecurityConfig.class)
class HealthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void healthEndpointReturnsUpStatus() throws Exception {
                mockMvc.perform(get("/api/health"))
                        .andExpect(status().isOk())
                        .andExpect(content().json("{\"status\":\"UP\"}"));
        }
}
