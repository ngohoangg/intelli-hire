package org.aibles.intellihire.authservice.auth.usecase;

import org.aibles.intellihire.authservice.auth.adapter.repository.RefreshTokenRepository;
import org.aibles.intellihire.authservice.auth.domain.RefreshTokenEntity;
import org.aibles.intellihire.authservice.auth.token.JwtTokenService;
import org.aibles.intellihire.authservice.auth.token.RefreshTokenClaims;
import org.aibles.intellihire.authservice.auth.usecase.command.LogoutCommand;
import org.aibles.intellihire.authservice.shared.service.SessionActivityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseImplTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtTokenService jwtTokenService;
    @Mock private SessionActivityService sessionActivityService;

    @InjectMocks
    private LogoutUseCaseImpl logoutUseCase;

    @Test
    void execute_singleLogout_revokesToken() {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setTokenId("tid");
        token.setAccountId(1L);
        token.setExpiresAt(LocalDateTime.now().plusDays(7));

        when(jwtTokenService.parseAndValidateRefreshToken(anyString()))
                .thenReturn(new RefreshTokenClaims("tid", 1L, "test@example.com"));
        when(refreshTokenRepository.findByTokenId("tid")).thenReturn(Optional.of(token));

        logoutUseCase.execute(new LogoutCommand("refresh-token"));

        verify(refreshTokenRepository).save(any());
    }

    @Test
    void execute_singleLogout_deletesRedisSession() {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setTokenId("tid");
        token.setAccountId(1L);
        token.setExpiresAt(LocalDateTime.now().plusDays(7));

        when(jwtTokenService.parseAndValidateRefreshToken(anyString()))
                .thenReturn(new RefreshTokenClaims("tid", 1L, "test@example.com"));
        when(refreshTokenRepository.findByTokenId("tid")).thenReturn(Optional.of(token));

        logoutUseCase.execute(new LogoutCommand("refresh-token"));

        verify(sessionActivityService).delete(1L);
    }

    @Test
    void logoutAll_revokesAllTokens() {
        when(refreshTokenRepository.revokeAllByAccountId(eq(1L), any(), eq("logout_all")))
                .thenReturn(3);

        logoutUseCase.logoutAll(1L);

        verify(refreshTokenRepository).revokeAllByAccountId(eq(1L), any(), eq("logout_all"));
    }

    @Test
    void logoutAll_deletesRedisSession() {
        when(refreshTokenRepository.revokeAllByAccountId(eq(1L), any(), eq("logout_all")))
                .thenReturn(3);

        logoutUseCase.logoutAll(1L);

        verify(sessionActivityService).delete(1L);
    }
}
