package org.aibles.intellihire.authservice.admin.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionResponseDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String category;

    @JsonProperty("is_active")
    private Boolean isActive;
}
