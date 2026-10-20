package org.aibles.intellihire.authservice.profile.adapter.web;

import org.aibles.intellihire.authservice.profile.adapter.web.dto.ProfileResponseDTO;
import org.aibles.intellihire.authservice.profile.adapter.web.dto.UpdateProfileRequestDTO;
import org.aibles.intellihire.authservice.profile.domain.UserProfileEntity;
import org.aibles.intellihire.authservice.profile.usecase.GetProfileUseCase;
import org.aibles.intellihire.authservice.profile.usecase.UpdateProfileUseCase;
import org.aibles.intellihire.authservice.profile.usecase.command.UpdateProfileCommand;
import org.aibles.intellihire.authservice.shared.config.JwtAuthenticationToken;
import org.aibles.intellihire.authservice.shared.dto.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private GetProfileUseCase getProfileUseCase;

    @Mock
    private UpdateProfileUseCase updateProfileUseCase;

    @InjectMocks
    private ProfileController profileController;

    @Test
    @DisplayName("Should return user profile successfully for authenticated user")
    void getMyProfile_success() {
        JwtAuthenticationToken auth = mock(JwtAuthenticationToken.class);
        when(auth.getAccountId()).thenReturn(1L);

        UserProfileEntity profile = new UserProfileEntity();
        profile.setId(10L);
        profile.setAccountId(1L);
        profile.setFirstName("John");
        profile.setLastName("Doe");

        when(getProfileUseCase.execute(1L)).thenReturn(profile);

        ResponseEntity<ApiResponse<ProfileResponseDTO>> response = profileController.getMyProfile(auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getId()).isEqualTo(10L);
        assertThat(response.getBody().getData().getFirstName()).isEqualTo("John");
        assertThat(response.getBody().getData().getLastName()).isEqualTo("Doe");
        assertThat(response.getBody().getData().getFullName()).isEqualTo("John Doe");

        verify(getProfileUseCase).execute(1L);
    }

    @Test
    @DisplayName("Should update user profile successfully for authenticated user")
    void updateMyProfile_success() {
        JwtAuthenticationToken auth = mock(JwtAuthenticationToken.class);
        when(auth.getAccountId()).thenReturn(1L);

        UpdateProfileRequestDTO request = UpdateProfileRequestDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phone("0123456789")
                .birthOfDate(LocalDate.of(1990, 10, 15))
                .build();

        UserProfileEntity updatedProfile = new UserProfileEntity();
        updatedProfile.setId(10L);
        updatedProfile.setAccountId(1L);
        updatedProfile.setFirstName("Jane");
        updatedProfile.setLastName("Smith");
        updatedProfile.setPhone("0123456789");
        updatedProfile.setBirthOfDate(LocalDate.of(1990, 10, 15));

        when(updateProfileUseCase.execute(any(UpdateProfileCommand.class))).thenReturn(updatedProfile);

        ResponseEntity<ApiResponse<ProfileResponseDTO>> response = profileController.updateMyProfile(auth, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getFirstName()).isEqualTo("Jane");
        assertThat(response.getBody().getData().getLastName()).isEqualTo("Smith");
        assertThat(response.getBody().getData().getPhone()).isEqualTo("0123456789");

        verify(updateProfileUseCase).execute(any(UpdateProfileCommand.class));
    }
}
