package org.aibles.intellihire.authservice.account.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteAccountRequestDTO {
    @NotBlank(message = "Password is required to confirm account deletion")
    private String password;
}
