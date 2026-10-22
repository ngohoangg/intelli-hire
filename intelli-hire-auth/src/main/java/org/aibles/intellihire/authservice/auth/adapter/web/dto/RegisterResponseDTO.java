package org.aibles.intellihire.authservice.auth.adapter.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponseDTO {
    private Long accountId;
    private String email;
    private String username;
    private String message;
}
