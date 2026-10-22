package org.aibles.intellihire.authservice.shared.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application.rate-limit")
public class RateLimitProperties {
    private boolean enabled = true;
    private int defaultCapacity = 60;
    private int defaultRefillTokens = 60;
    private int defaultRefillSeconds = 60;
}
