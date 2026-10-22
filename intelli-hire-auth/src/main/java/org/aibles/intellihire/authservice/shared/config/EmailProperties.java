package org.aibles.intellihire.authservice.shared.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application.email")
public class EmailProperties {
    private String from = "noreply@intellihire.com";
    private String baseUrl = "http://localhost:4200";
}
