package org.aibles.intellihire.authservice.admin.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.aibles.intellihire.authservice.account.domain.AccountStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class AccountResponseDTO {

    private Long id;
    private String email;
    private String username;
    private AccountStatus status;

    @JsonProperty("failed_login_attempts")
    private Integer failedLoginAttempts;

    @JsonProperty("last_login_at")
    private LocalDateTime lastLoginAt;

    @JsonProperty("locked_at")
    private LocalDateTime lockedAt;

    @JsonProperty("locked_reason")
    private String lockedReason;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("is_super_admin")
    private boolean isSuperAdmin;
}
