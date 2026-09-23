package org.aibles.intellihire.authservice.profile.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.profile.adapter.web.dto.ProfileResponseDTO;
import org.aibles.intellihire.authservice.profile.adapter.web.dto.UpdateProfileRequestDTO;
import org.aibles.intellihire.authservice.profile.domain.UserProfileEntity;
import org.aibles.intellihire.authservice.profile.usecase.GetProfileUseCase;
import org.aibles.intellihire.authservice.profile.usecase.UpdateProfileUseCase;
import org.aibles.intellihire.authservice.profile.usecase.command.UpdateProfileCommand;
import org.aibles.intellihire.authservice.shared.config.JwtAuthenticationToken;
import org.aibles.intellihire.authservice.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final GetProfileUseCase getProfileUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Returns the profile of the authenticated user")
    public ResponseEntity<ApiResponse<ProfileResponseDTO>> getMyProfile(
            @AuthenticationPrincipal JwtAuthenticationToken authentication
    ) {
        log.debug("Getting profile for account: {}", authentication.getAccountId());

        UserProfileEntity profile = getProfileUseCase.execute(authentication.getAccountId());

        return ResponseEntity.ok(ApiResponse.success(toResponseDTO(profile)));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile", description = "Updates the profile of the authenticated user")
    public ResponseEntity<ApiResponse<ProfileResponseDTO>> updateMyProfile(
            @AuthenticationPrincipal JwtAuthenticationToken authentication,
            @Valid @RequestBody UpdateProfileRequestDTO requestDTO
    ) {
        log.debug("Updating profile for account: {}", authentication.getAccountId());

        UpdateProfileCommand command = new UpdateProfileCommand(
                authentication.getAccountId(),
                requestDTO.getFirstName(),
                requestDTO.getMiddleName(),
                requestDTO.getLastName(),
                requestDTO.getAvatarUrl(),
                requestDTO.getPhone(),
                requestDTO.getAddress(),
                requestDTO.getBirthOfDate(),
                requestDTO.getGender(),
                requestDTO.getGithubUrl(),
                requestDTO.getLinkedInUrl()
        );

        UserProfileEntity profile = updateProfileUseCase.execute(command);

        return ResponseEntity.ok(ApiResponse.success(toResponseDTO(profile)));
    }

    private ProfileResponseDTO toResponseDTO(UserProfileEntity profile) {
        return ProfileResponseDTO.builder()
                .id(profile.getId())
                .accountId(profile.getAccountId())
                .firstName(profile.getFirstName())
                .middleName(profile.getMiddleName())
                .lastName(profile.getLastName())
                .fullName(profile.getFullName())
                .avatarUrl(profile.getAvatarUrl())
                .phone(profile.getPhone())
                .address(profile.getAddress())
                .birthOfDate(profile.getBirthOfDate())
                .gender(profile.getGender())
                .githubUrl(profile.getGithubUrl())
                .linkedInUrl(profile.getLinkedInUrl())
                .build();
    }
}
