package org.aibles.intellihire.authservice.shared.config.properties;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "application.auth.password")
public class PasswordProperties {

    @Min(value = 6, message = "Minimum password length must be at least 6")
    private int minLength = 8;

    private boolean requireUppercase = true;
    private boolean requireLowercase = true;
    private boolean requireDigit = true;
    private boolean requireSpecial = false;

    @Min(value = 1, message = "Max failed attempts must be at least 1")
    private int maxFailedAttempts = 5;

    @Min(value = 1, message = "Lock duration must be at least 1 minute")
    private int lockDurationMinutes = 30;
}
