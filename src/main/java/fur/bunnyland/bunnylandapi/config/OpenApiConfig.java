package fur.bunnyland.bunnylandapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bunnylandOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🐇 Bunnyland API")
                        .description("Community platform for bunny owners 🐰 — connect, share care tips, and help each other with bunny-sitting.")
                        .version("v0.1")
                        .contact(new Contact()
                                .name("Kateryna Yashnyk")
                                .url("https://github.com/lunaridelmar")
                                .email("aolanikauhilani@gmail.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Dev")
                ));
    }
}
