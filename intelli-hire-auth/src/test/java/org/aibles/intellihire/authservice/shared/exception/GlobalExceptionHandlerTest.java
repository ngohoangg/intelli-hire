package org.aibles.intellihire.authservice.shared.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle BaseException correctly")
    void shouldHandleBaseException() {
        BaseException ex = new BaseException(ErrorCode.INVALID_CREDENTIALS, "Extra details");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBaseException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("AUTH_1001");
        assertThat(response.getBody().getMessage()).isEqualTo("Email/tên đăng nhập hoặc mật khẩu không đúng");
        assertThat(response.getBody().getDetails()).isEqualTo("Extra details");
        assertThat(response.getBody().getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("Should handle AccessDeniedException correctly")
    void shouldHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access is denied");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.ACCESS_DENIED.getCode());
        assertThat(response.getBody().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("Should handle AuthenticationException correctly")
    void shouldHandleAuthenticationException() {
        AuthenticationException ex = new AuthenticationException("Bad token") {};
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.INVALID_TOKEN.getCode());
        assertThat(response.getBody().getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("Should handle generic Exception correctly")
    void shouldHandleGenericException() {
        Exception ex = new RuntimeException("Unexpected database failure");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getCode());
        assertThat(response.getBody().getStatus()).isEqualTo(500);
    }
}
