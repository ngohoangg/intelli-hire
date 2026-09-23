package org.aibles.intellihirequestion.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Intelli Hire Question API")
                        .version("1.0.0")
                        .description("API documentation for Intelli Hire Question Service")
                        .contact(new Contact()
                                .name("Aibles")
                                .email("contact@aibles.com"))
                        .license(new License()
                                .name("Private License")))
                .servers(List.of(
                        new Server().url("http://localhost:8080" + contextPath).description("Local Development Server")
                ));
    }
}
