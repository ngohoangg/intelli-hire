package org.aibles.intellihire.authservice.admin.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssignRoleRequestDTO {

    @NotNull(message = "Role ID is required")
    @JsonProperty("role_id")
    private Long roleId;

    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;
}
