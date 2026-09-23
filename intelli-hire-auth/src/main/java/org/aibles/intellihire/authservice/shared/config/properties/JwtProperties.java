package org.aibles.intellihire.authservice.shared.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "application.auth.jwt")
public class JwtProperties {

    @NotBlank(message = "JWT secret is required")
    private String secret;

    @NotBlank(message = "JWT issuer is required")
    private String issuer;

    @NotBlank(message = "Access token audience is required")
    private String accessTokenAudience;

    @NotBlank(message = "Refresh token audience is required")
    private String refreshTokenAudience;

    @Min(value = 60, message = "Access token expiration must be at least 60 seconds")
    private long accessTokenExpirationSeconds = 900; // 15 minutes

    @Min(value = 300, message = "Refresh token expiration must be at least 300 seconds")
    private long refreshTokenExpirationSeconds = 604800; // 7 days
}
