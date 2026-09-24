package org.aibles.intellihire.authservice.admin.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateRoleRequestDTO {

    @Size(max = 50, message = "Tên vai trò không được vượt quá 50 ký tự")
    private String name;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String description;

    @JsonProperty("is_active")
    private Boolean isActive;
}
