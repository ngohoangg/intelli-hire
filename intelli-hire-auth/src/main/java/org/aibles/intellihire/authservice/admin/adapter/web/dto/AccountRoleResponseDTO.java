package org.aibles.intellihire.authservice.admin.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AccountRoleResponseDTO {

    @JsonProperty("role_id")
    private Long roleId;

    @JsonProperty("role_name")
    private String roleName;

    @JsonProperty("assigned_at")
    private LocalDateTime assignedAt;

    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;
}
