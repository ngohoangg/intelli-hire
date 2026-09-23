package org.aibles.intellihireinterview.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBaseException_sessionNotFound_returns404() {
        var response = handler.handleBaseException(new SessionNotFoundException(1L));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo("INT_001");
    }

    @Test
    void handleBaseException_sessionNotActive_returns409() {
        var response = handler.handleBaseException(new SessionNotActiveException(1L));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getErrorCode()).isEqualTo("INT_002");
    }

    @Test
    void handleIllegalState_returns409() {
        var response = handler.handleIllegalState(new IllegalStateException("duplicate answer"));
        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody().isSuccess()).isFalse();
    }
}
