package org.aibles.intellihire.authservice.auth.usecase;

import org.aibles.intellihire.authservice.auth.adapter.repository.RefreshTokenRepository;
import org.aibles.intellihire.authservice.auth.domain.InvalidTokenException;
import org.aibles.intellihire.authservice.auth.domain.RefreshTokenEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevokeSessionUseCaseImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RevokeSessionUseCaseImpl revokeSessionUseCase;

    @Test
    void execute_success() {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setId(10L);
        token.setAccountId(1L);

        when(refreshTokenRepository.findById(10L)).thenReturn(Optional.of(token));

        revokeSessionUseCase.execute(1L, 10L);

        assertThat(token.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
    }

    @Test
    void execute_accountMismatch_throwsException() {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setId(10L);
        token.setAccountId(2L);

        when(refreshTokenRepository.findById(10L)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> revokeSessionUseCase.execute(1L, 10L))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void execute_sessionNotFound_throwsException() {
        when(refreshTokenRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> revokeSessionUseCase.execute(1L, 10L))
                .isInstanceOf(InvalidTokenException.class);
    }
}
