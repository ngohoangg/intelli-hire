package org.aibles.intellihire.authservice.auth.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.auth.adapter.web.dto.*;
import org.aibles.intellihire.authservice.auth.usecase.*;
import org.aibles.intellihire.authservice.auth.usecase.command.*;
import org.aibles.intellihire.authservice.shared.config.JwtAuthenticationToken;
import org.aibles.intellihire.authservice.shared.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final LogoutUseCase logoutUseCase;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new account", description = "Creates a new user account with email and password")
    public ResponseEntity<ApiResponse<RegisterResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO requestDTO,
            HttpServletRequest request
    ) {
        log.info("Registration request for email: {}", requestDTO.getEmail());

        RegisterCommand command = new RegisterCommand(
                requestDTO.getEmail(),
                requestDTO.getUsername(),
                requestDTO.getPassword(),
                requestDTO.getConfirmPassword(),
                requestDTO.getFirstName(),
                requestDTO.getLastName(),
                getClientIp(request),
                request.getHeader("User-Agent")
        );

        RegisterUseCase.RegisterResponse response = registerUseCase.execute(command);

        RegisterResponseDTO responseDTO = RegisterResponseDTO.builder()
                .accountId(response.accountId())
                .email(response.email())
                .username(response.username())
                .message(response.message())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(responseDTO));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates user and returns JWT tokens")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO requestDTO,
            HttpServletRequest request
    ) {
        log.info("Login request for identifier: {}", requestDTO.getIdentifier());

        LoginCommand command = new LoginCommand(
                requestDTO.getIdentifier(),
                requestDTO.getPassword(),
                getClientIp(request),
                request.getHeader("User-Agent")
        );

        LoginUseCase.LoginResponse response = loginUseCase.execute(command);

        return ResponseEntity.ok(ApiResponse.success(toLoginResponseDTO(response)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh tokens", description = "Exchanges a refresh token for a new token pair")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> refresh(
            @Valid @RequestBody RefreshTokenRequestDTO requestDTO
    ) {
        log.debug("Token refresh request");

        RefreshTokenCommand command = new RefreshTokenCommand(requestDTO.getRefreshToken());
        LoginUseCase.LoginResponse response = refreshTokenUseCase.execute(command);

        return ResponseEntity.ok(ApiResponse.success(toLoginResponseDTO(response)));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Sends a password reset link to the user's email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO requestDTO,
            HttpServletRequest request
    ) {
        log.info("Forgot password request for email: {}", requestDTO.getEmail());

        ForgotPasswordCommand command = new ForgotPasswordCommand(
                requestDTO.getEmail(),
                getClientIp(request)
        );

        ForgotPasswordUseCase.ForgotPasswordResponse response = forgotPasswordUseCase.execute(command);

        return ResponseEntity.ok(ApiResponse.success(response.message()));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Resets the user's password using a valid reset token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO requestDTO
    ) {
        log.debug("Password reset request");

        ResetPasswordCommand command = new ResetPasswordCommand(
                requestDTO.getToken(),
                requestDTO.getNewPassword()
        );

        ResetPasswordUseCase.ResetPasswordResponse response = resetPasswordUseCase.execute(command);

        return ResponseEntity.ok(ApiResponse.success(response.message()));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verifies the user's email using a verification token")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequestDTO requestDTO
    ) {
        log.debug("Email verification request");

        VerifyEmailCommand command = new VerifyEmailCommand(requestDTO.getToken());
        VerifyEmailUseCase.VerifyEmailResponse response = verifyEmailUseCase.execute(command);

        return ResponseEntity.ok(ApiResponse.success(response.message()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revokes the refresh token",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequestDTO requestDTO
    ) {
        log.debug("Logout request");

        LogoutCommand command = new LogoutCommand(requestDTO.getRefreshToken());
        logoutUseCase.execute(command);

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully."));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all devices", description = "Revokes all refresh tokens for the current user",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @AuthenticationPrincipal JwtAuthenticationToken authentication
    ) {
        log.debug("Logout all request for account: {}", authentication.getAccountId());

        logoutUseCase.logoutAll(authentication.getAccountId());

        return ResponseEntity.ok(ApiResponse.success("Logged out from all devices successfully."));
    }

    private LoginResponseDTO toLoginResponseDTO(LoginUseCase.LoginResponse response) {
        return LoginResponseDTO.builder()
                .accessToken(response.accessToken())
                .refreshToken(response.refreshToken())
                .tokenType(response.tokenType())
                .expiresIn(response.expiresIn())
                .refreshExpiresIn(response.refreshExpiresIn())
                .accountId(response.accountId())
                .email(response.email())
                .username(response.username())
                .roles(response.roles())
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
