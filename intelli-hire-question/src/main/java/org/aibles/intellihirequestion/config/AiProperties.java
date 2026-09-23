package org.aibles.intellihirequestion.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private String provider = "openrouter";
    private OpenRouterConfig openrouter = new OpenRouterConfig();

    @Data
    public static class OpenRouterConfig {
        private String apiKey = "";
        private String model = "x-ai/grok-4.1-fast";
        private int timeoutSeconds = 60;
        private String siteUrl = "https://intellihire.dev";
        private String siteName = "IntelliHire";
    }
}
