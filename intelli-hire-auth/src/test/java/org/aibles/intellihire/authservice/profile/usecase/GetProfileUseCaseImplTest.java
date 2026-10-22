package org.aibles.intellihire.authservice.profile.usecase;

import org.aibles.intellihire.authservice.profile.adapter.repository.UserProfileRepository;
import org.aibles.intellihire.authservice.profile.domain.ProfileNotFoundException;
import org.aibles.intellihire.authservice.profile.domain.UserProfileEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProfileUseCaseImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private GetProfileUseCaseImpl getProfileUseCase;

    @Test
    @DisplayName("Should return user profile when profile exists")
    void shouldReturnProfileWhenFound() {
        Long accountId = 1L;
        UserProfileEntity profile = new UserProfileEntity();
        profile.setId(10L);
        profile.setAccountId(accountId);
        profile.setFirstName("John");
        profile.setLastName("Doe");

        when(userProfileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));

        UserProfileEntity result = getProfileUseCase.execute(accountId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        verify(userProfileRepository).findByAccountId(accountId);
    }

    @Test
    @DisplayName("Should throw ProfileNotFoundException when profile does not exist")
    void shouldThrowProfileNotFoundExceptionWhenNotFound() {
        Long accountId = 999L;
        when(userProfileRepository.findByAccountId(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getProfileUseCase.execute(accountId))
                .isInstanceOf(ProfileNotFoundException.class);

        verify(userProfileRepository).findByAccountId(accountId);
    }
}
