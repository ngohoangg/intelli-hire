package org.aibles.intellihire.authservice.auth.adapter.web;

import org.aibles.intellihire.authservice.auth.adapter.web.dto.SessionResponseDTO;
import org.aibles.intellihire.authservice.auth.domain.RefreshTokenEntity;
import org.aibles.intellihire.authservice.auth.usecase.ListActiveSessionsUseCase;
import org.aibles.intellihire.authservice.auth.usecase.RevokeSessionUseCase;
import org.aibles.intellihire.authservice.shared.config.JwtAuthenticationToken;
import org.aibles.intellihire.authservice.shared.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

    @Mock
    private ListActiveSessionsUseCase listActiveSessionsUseCase;

    @Mock
    private RevokeSessionUseCase revokeSessionUseCase;

    @InjectMocks
    private SessionController sessionController;

    @Test
    void listSessions_success() {
        JwtAuthenticationToken auth = mock(JwtAuthenticationToken.class);
        when(auth.getAccountId()).thenReturn(1L);

        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setId(10L);
        token.setDeviceInfo("Chrome / MacOS");
        token.setIpAddress("127.0.0.1");
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusDays(7));

        when(listActiveSessionsUseCase.execute(1L)).thenReturn(List.of(token));

        ResponseEntity<ApiResponse<List<SessionResponseDTO>>> response = sessionController.listSessions(auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).getId()).isEqualTo(10L);
        assertThat(response.getBody().getData().get(0).getDeviceInfo()).isEqualTo("Chrome / MacOS");
    }

    @Test
    void revokeSession_success() {
        JwtAuthenticationToken auth = mock(JwtAuthenticationToken.class);
        when(auth.getAccountId()).thenReturn(1L);

        ResponseEntity<ApiResponse<Void>> response = sessionController.revokeSession(auth, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(revokeSessionUseCase).execute(1L, 10L);
    }
}
