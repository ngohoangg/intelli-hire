package org.aibles.intellihire.authservice.auth.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.auth.adapter.web.dto.SessionResponseDTO;
import org.aibles.intellihire.authservice.auth.domain.RefreshTokenEntity;
import org.aibles.intellihire.authservice.auth.usecase.ListActiveSessionsUseCase;
import org.aibles.intellihire.authservice.auth.usecase.RevokeSessionUseCase;
import org.aibles.intellihire.authservice.shared.config.JwtAuthenticationToken;
import org.aibles.intellihire.authservice.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/auth/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions", description = "Session management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SessionController {

    private final ListActiveSessionsUseCase listActiveSessionsUseCase;
    private final RevokeSessionUseCase revokeSessionUseCase;

    @GetMapping
    @Operation(summary = "List active sessions", description = "Returns all active sessions for the authenticated user")
    public ResponseEntity<ApiResponse<List<SessionResponseDTO>>> listSessions(
            @AuthenticationPrincipal JwtAuthenticationToken authentication
    ) {
        log.debug("Listing sessions for account: {}", authentication.getAccountId());

        List<RefreshTokenEntity> sessions = listActiveSessionsUseCase.execute(authentication.getAccountId());

        List<SessionResponseDTO> response = sessions.stream()
                .map(this::toSessionResponseDTO)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Revoke session", description = "Revokes a specific session by its ID")
    public ResponseEntity<ApiResponse<Void>> revokeSession(
            @AuthenticationPrincipal JwtAuthenticationToken authentication,
            @PathVariable Long sessionId
    ) {
        log.info("Revoking session {} for account: {}", sessionId, authentication.getAccountId());

        revokeSessionUseCase.execute(authentication.getAccountId(), sessionId);

        return ResponseEntity.ok(ApiResponse.success("Session revoked successfully."));
    }

    private SessionResponseDTO toSessionResponseDTO(RefreshTokenEntity token) {
        return SessionResponseDTO.builder()
                .id(token.getId())
                .deviceInfo(token.getDeviceInfo())
                .ipAddress(token.getIpAddress())
                .createdAt(token.getCreatedAt())
                .expiresAt(token.getExpiresAt())
                .build();
    }
}
