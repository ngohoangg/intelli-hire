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
import org.aibles.intellihire.authservice.shared.config.JwtAuthenticationToken;
import org.aibles.intellihire.authservice.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final GetProfileUseCase getProfileUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Retrieves the profile of the authenticated user")
    public ResponseEntity<ApiResponse<ProfileResponseDTO>> getMyProfile(
            @AuthenticationPrincipal JwtAuthenticationToken authentication
    ) {
        log.debug("Get profile request for account: {}", authentication.getAccountId());
        UserProfileEntity profile = getProfileUseCase.execute(authentication.getAccountId());
        return ResponseEntity.ok(ApiResponse.success(ProfileResponseDTO.fromEntity(profile)));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile", description = "Updates profile details for the authenticated user")
    public ResponseEntity<ApiResponse<ProfileResponseDTO>> updateMyProfile(
            @AuthenticationPrincipal JwtAuthenticationToken authentication,
            @Valid @RequestBody UpdateProfileRequestDTO request
    ) {
        log.info("Update profile request for account: {}", authentication.getAccountId());
        UserProfileEntity updatedProfile = updateProfileUseCase.execute(request.toCommand(authentication.getAccountId()));
        return ResponseEntity.ok(ApiResponse.success("Hồ sơ đã được cập nhật thành công", ProfileResponseDTO.fromEntity(updatedProfile)));
    }
}
