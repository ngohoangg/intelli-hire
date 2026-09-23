package org.aibles.intellihiregrading.config;

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
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Intelli Hire Grading API")
                        .version("1.0.0")
                        .description("AI-powered interview answer grading service")
                        .contact(new Contact().name("Aibles").email("contact@aibles.com")))
                .servers(List.of(new Server().url("http://localhost:8084/api")));
    }
}
