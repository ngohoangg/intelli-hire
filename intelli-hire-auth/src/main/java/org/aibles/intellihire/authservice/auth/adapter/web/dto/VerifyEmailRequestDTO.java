package org.aibles.intellihire.authservice.auth.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyEmailRequestDTO {

    @NotBlank(message = "Token là bắt buộc")
    private String token;
}
