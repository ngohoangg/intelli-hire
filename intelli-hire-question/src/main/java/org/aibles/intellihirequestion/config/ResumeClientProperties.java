package org.aibles.intellihirequestion.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "clients.resume")
public class ResumeClientProperties {
    private String baseUrl = "http://localhost:8082";
    private int timeoutSeconds = 30;
    private String questionContextPath = "/api/v1/resumes/{resumeId}/profile";
}
