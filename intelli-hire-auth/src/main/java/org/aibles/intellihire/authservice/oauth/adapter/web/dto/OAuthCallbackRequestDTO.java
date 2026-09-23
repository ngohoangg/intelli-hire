package org.aibles.intellihire.authservice.oauth.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OAuthCallbackRequestDTO {

    @NotBlank(message = "Authorization code is required")
    private String code;

    @NotBlank(message = "State is required")
    private String state;
}
