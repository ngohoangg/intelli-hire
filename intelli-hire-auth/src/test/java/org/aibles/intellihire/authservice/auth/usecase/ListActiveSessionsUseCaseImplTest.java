package org.aibles.intellihire.authservice.auth.usecase;

import org.aibles.intellihire.authservice.auth.adapter.repository.RefreshTokenRepository;
import org.aibles.intellihire.authservice.auth.domain.RefreshTokenEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListActiveSessionsUseCaseImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private ListActiveSessionsUseCaseImpl listActiveSessionsUseCase;

    @Test
    void execute_returnsActiveSessions() {
        RefreshTokenEntity session1 = new RefreshTokenEntity();
        session1.setId(10L);
        session1.setAccountId(1L);

        RefreshTokenEntity session2 = new RefreshTokenEntity();
        session2.setId(11L);
        session2.setAccountId(1L);

        when(refreshTokenRepository.findActiveByAccountId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(session1, session2));

        List<RefreshTokenEntity> result = listActiveSessionsUseCase.execute(1L);

        assertThat(result).hasSize(2);
        verify(refreshTokenRepository).findActiveByAccountId(eq(1L), any(LocalDateTime.class));
    }
}
