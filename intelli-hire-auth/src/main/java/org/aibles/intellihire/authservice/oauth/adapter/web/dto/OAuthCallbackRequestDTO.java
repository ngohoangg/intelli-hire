package org.aibles.intellihire.authservice.oauth.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OAuthCallbackRequestDTO {

    @NotBlank(message = "Mã ủy quyền là bắt buộc")
    private String code;

    @NotBlank(message = "State là bắt buộc")
    private String state;
}
