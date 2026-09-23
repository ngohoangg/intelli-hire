package org.aibles.intellihire.authservice.admin.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoleResponseDTO {

    private Long id;
    private String name;
    private String description;

    @JsonProperty("is_system")
    private Boolean isSystem;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
