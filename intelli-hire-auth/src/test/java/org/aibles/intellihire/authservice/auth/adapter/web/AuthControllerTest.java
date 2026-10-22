package org.aibles.intellihire.authservice.auth.adapter.web;

import org.aibles.intellihire.authservice.auth.adapter.web.dto.*;
import org.aibles.intellihire.authservice.auth.usecase.*;
import org.aibles.intellihire.authservice.auth.usecase.command.*;
import org.aibles.intellihire.authservice.shared.config.JwtAuthenticationToken;
import org.aibles.intellihire.authservice.shared.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private RegisterUseCase registerUseCase;
    @Mock private LoginUseCase loginUseCase;
    @Mock private RefreshTokenUseCase refreshTokenUseCase;
    @Mock private ForgotPasswordUseCase forgotPasswordUseCase;
    @Mock private ResetPasswordUseCase resetPasswordUseCase;
    @Mock private VerifyEmailUseCase verifyEmailUseCase;
    @Mock private LogoutUseCase logoutUseCase;

    @InjectMocks
    private AuthController authController;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0");
        request.setRemoteAddr("127.0.0.1");
    }

    @Test
    void register_success() {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO();
        requestDTO.setEmail("test@example.com");
        requestDTO.setUsername("testuser");
        requestDTO.setPassword("Password1");
        requestDTO.setConfirmPassword("Password1");

        when(registerUseCase.execute(any(RegisterCommand.class)))
                .thenReturn(new RegisterUseCase.RegisterResponse(1L, "test@example.com", "testuser", "Success"));

        ResponseEntity<ApiResponse<RegisterResponseDTO>> response = authController.register(requestDTO, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getAccountId()).isEqualTo(1L);
    }

    @Test
    void login_success() {
        LoginRequestDTO requestDTO = new LoginRequestDTO();
        requestDTO.setIdentifier("testuser");
        requestDTO.setPassword("Password1");

        when(loginUseCase.execute(any(LoginCommand.class)))
                .thenReturn(new LoginUseCase.LoginResponse(
                        "access-token", "refresh-token", "Bearer", 900, 604800,
                        1L, "test@example.com", "testuser", List.of("USER")
                ));

        ResponseEntity<ApiResponse<LoginResponseDTO>> response = authController.login(requestDTO, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getAccessToken()).isEqualTo("access-token");
    }

    @Test
    void refresh_success() {
        RefreshTokenRequestDTO requestDTO = new RefreshTokenRequestDTO();
        requestDTO.setRefreshToken("old-refresh-token");

        when(refreshTokenUseCase.execute(any(RefreshTokenCommand.class)))
                .thenReturn(new LoginUseCase.LoginResponse(
                        "new-access", "new-refresh", "Bearer", 900, 604800,
                        1L, "test@example.com", "testuser", List.of("USER")
                ));

        ResponseEntity<ApiResponse<LoginResponseDTO>> response = authController.refresh(requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getAccessToken()).isEqualTo("new-access");
    }

    @Test
    void forgotPassword_success() {
        ForgotPasswordRequestDTO requestDTO = new ForgotPasswordRequestDTO();
        requestDTO.setEmail("test@example.com");

        when(forgotPasswordUseCase.execute(any(ForgotPasswordCommand.class)))
                .thenReturn(new ForgotPasswordUseCase.ForgotPasswordResponse("Email sent"));

        ResponseEntity<ApiResponse<Void>> response = authController.forgotPassword(requestDTO, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Email sent");
    }

    @Test
    void resetPassword_success() {
        ResetPasswordRequestDTO requestDTO = new ResetPasswordRequestDTO();
        requestDTO.setToken("reset-token");
        requestDTO.setNewPassword("NewPassword1");

        when(resetPasswordUseCase.execute(any(ResetPasswordCommand.class)))
                .thenReturn(new ResetPasswordUseCase.ResetPasswordResponse("Password reset successful"));

        ResponseEntity<ApiResponse<Void>> response = authController.resetPassword(requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Password reset successful");
    }

    @Test
    void verifyEmail_success() {
        VerifyEmailRequestDTO requestDTO = new VerifyEmailRequestDTO();
        requestDTO.setToken("verify-token");

        when(verifyEmailUseCase.execute(any(VerifyEmailCommand.class)))
                .thenReturn(new VerifyEmailUseCase.VerifyEmailResponse("Email verified"));

        ResponseEntity<ApiResponse<Void>> response = authController.verifyEmail(requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Email verified");
    }

    @Test
    void logout_success() {
        RefreshTokenRequestDTO requestDTO = new RefreshTokenRequestDTO();
        requestDTO.setRefreshToken("refresh-token");

        ResponseEntity<ApiResponse<Void>> response = authController.logout(requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(logoutUseCase).execute(any(LogoutCommand.class));
    }

    @Test
    void logoutAll_success() {
        JwtAuthenticationToken authentication = mock(JwtAuthenticationToken.class);
        when(authentication.getAccountId()).thenReturn(1L);

        ResponseEntity<ApiResponse<Void>> response = authController.logoutAll(authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(logoutUseCase).logoutAll(1L);
    }
}
