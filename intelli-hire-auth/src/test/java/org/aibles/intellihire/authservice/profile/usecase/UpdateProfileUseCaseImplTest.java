package org.aibles.intellihire.authservice.profile.usecase;

import org.aibles.intellihire.authservice.profile.adapter.repository.UserProfileRepository;
import org.aibles.intellihire.authservice.profile.domain.UserProfileEntity;
import org.aibles.intellihire.authservice.profile.usecase.command.UpdateProfileCommand;
import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateProfileUseCaseImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UpdateProfileUseCaseImpl updateProfileUseCase;

    @Test
    @DisplayName("Should successfully update profile fields")
    void shouldUpdateProfileFieldsSuccessfully() {
        Long accountId = 1L;
        UserProfileEntity existing = new UserProfileEntity();
        existing.setId(5L);
        existing.setAccountId(accountId);
        existing.setFirstName("Old");
        existing.setLastName("Name");

        when(userProfileRepository.findByAccountId(accountId)).thenReturn(Optional.of(existing));
        when(userProfileRepository.findByPhone("0987654321")).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileCommand command = new UpdateProfileCommand(
                accountId,
                "NewFirst",
                "Middle",
                "NewLast",
                "https://example.com/avatar.jpg",
                "0987654321",
                "123 Street, City",
                LocalDate.of(1995, 5, 20),
                "MALE",
                "https://github.com/newfirst",
                "https://linkedin.com/in/newfirst"
        );

        UserProfileEntity updated = updateProfileUseCase.execute(command);

        assertThat(updated.getFirstName()).isEqualTo("NewFirst");
        assertThat(updated.getMiddleName()).isEqualTo("Middle");
        assertThat(updated.getLastName()).isEqualTo("NewLast");
        assertThat(updated.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
        assertThat(updated.getPhone()).isEqualTo("0987654321");
        assertThat(updated.getAddress()).isEqualTo("123 Street, City");
        assertThat(updated.getBirthOfDate()).isEqualTo(LocalDate.of(1995, 5, 20));
        assertThat(updated.getGender()).isEqualTo("MALE");
        assertThat(updated.getGithubUrl()).isEqualTo("https://github.com/newfirst");
        assertThat(updated.getLinkedInUrl()).isEqualTo("https://linkedin.com/in/newfirst");

        verify(userProfileRepository).save(existing);
    }

    @Test
    @DisplayName("Should throw exception when phone is already used by another account")
    void shouldThrowExceptionWhenPhoneUsedByAnotherAccount() {
        Long accountId = 1L;
        UserProfileEntity existing = new UserProfileEntity();
        existing.setId(5L);
        existing.setAccountId(accountId);

        UserProfileEntity otherAccountProfile = new UserProfileEntity();
        otherAccountProfile.setId(6L);
        otherAccountProfile.setAccountId(2L); // Different account
        otherAccountProfile.setPhone("0987654321");

        when(userProfileRepository.findByAccountId(accountId)).thenReturn(Optional.of(existing));
        when(userProfileRepository.findByPhone("0987654321")).thenReturn(Optional.of(otherAccountProfile));

        UpdateProfileCommand command = new UpdateProfileCommand(
                accountId,
                null, null, null, null,
                "0987654321",
                null, null, null, null, null
        );

        assertThatThrownBy(() -> updateProfileUseCase.execute(command))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("Số điện thoại đã được sử dụng");

        verify(userProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should allow updating profile with same phone belonging to current account")
    void shouldAllowSamePhoneForSameAccount() {
        Long accountId = 1L;
        UserProfileEntity existing = new UserProfileEntity();
        existing.setId(5L);
        existing.setAccountId(accountId);
        existing.setPhone("0987654321");

        when(userProfileRepository.findByAccountId(accountId)).thenReturn(Optional.of(existing));
        when(userProfileRepository.findByPhone("0987654321")).thenReturn(Optional.of(existing));
        when(userProfileRepository.save(any(UserProfileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileCommand command = new UpdateProfileCommand(
                accountId,
                "UpdatedName", null, null, null,
                "0987654321",
                null, null, null, null, null
        );

        UserProfileEntity updated = updateProfileUseCase.execute(command);
        assertThat(updated.getFirstName()).isEqualTo("UpdatedName");
        assertThat(updated.getPhone()).isEqualTo("0987654321");
    }

    @Test
    @DisplayName("Should lazily create profile if not existing")
    void shouldCreateProfileLazilyIfNotFound() {
        Long accountId = 100L;
        when(userProfileRepository.findByAccountId(accountId)).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileCommand command = new UpdateProfileCommand(
                accountId,
                "BrandNew", null, null, null,
                null, null, null, null, null, null
        );

        UserProfileEntity result = updateProfileUseCase.execute(command);
        assertThat(result.getAccountId()).isEqualTo(accountId);
        assertThat(result.getFirstName()).isEqualTo("BrandNew");
    }
}
