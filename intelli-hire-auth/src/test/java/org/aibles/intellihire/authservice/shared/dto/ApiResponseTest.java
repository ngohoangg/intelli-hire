package org.aibles.intellihire.authservice.shared.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    @DisplayName("Should create ApiResponse with data using ok(data)")
    void shouldCreateWithData() {
        String testData = "test-payload";
        ApiResponse<String> response = ApiResponse.ok(testData);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(testData);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should create ApiResponse with message and data using ok(message, data)")
    void shouldCreateWithMessageAndData() {
        String message = "Operation succeeded";
        Integer testData = 123;
        ApiResponse<Integer> response = ApiResponse.ok(message, testData);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getData()).isEqualTo(testData);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should create ApiResponse with message only using message(message)")
    void shouldCreateWithMessageOnly() {
        String message = "Created successfully";
        ApiResponse<Void> response = ApiResponse.message(message);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getData()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }
}
