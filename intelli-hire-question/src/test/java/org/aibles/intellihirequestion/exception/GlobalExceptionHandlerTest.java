package org.aibles.intellihirequestion.exception;

import org.aibles.intellihirequestion.dto.BaseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("NotFoundException should return 404 with error code")
    void handleBaseException_notFound_shouldReturn404() {
        NotFoundException ex = new NotFoundException(ErrorCode.QUE_001);

        ResponseEntity<BaseResponse<Void>> response = handler.handleBaseException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("QUE_001", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("DuplicateException should return 409 with error code")
    void handleBaseException_duplicate_shouldReturn409() {
        DuplicateException ex = new DuplicateException(ErrorCode.QUE_002);

        ResponseEntity<BaseResponse<Void>> response = handler.handleBaseException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("QUE_002", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("ValidationException should return 400 with error code")
    void handleBaseException_validation_shouldReturn400() {
        ValidationException ex = new ValidationException(ErrorCode.COM_001);

        ResponseEntity<BaseResponse<Void>> response = handler.handleBaseException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("COM_001", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("BadRequestException should return 400 with error code")
    void handleBaseException_badRequest_shouldReturn400() {
        BadRequestException ex = new BadRequestException(ErrorCode.COM_003);

        ResponseEntity<BaseResponse<Void>> response = handler.handleBaseException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("COM_003", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("MethodArgumentNotValidException should return 400 with field errors")
    void handleMethodArgumentNotValidException_shouldReturn400WithFieldErrors() throws NoSuchMethodException {
        BindingResult bindingResult = mock(BindingResult.class);
        org.springframework.validation.FieldError fieldError =
                new org.springframework.validation.FieldError("object", "name", "", false, null, null, "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        java.lang.reflect.Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("setUp");
        MethodParameter methodParameter = new MethodParameter(method, -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<BaseResponse<Void>> response = handler.handleMethodArgumentNotValidException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("COM_001", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getErrors());
        assertEquals(1, response.getBody().getErrors().size());
        assertEquals("name", response.getBody().getErrors().get(0).getField());
        assertEquals("must not be blank", response.getBody().getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("ConstraintViolationException should return 400 with field errors")
    void handleConstraintViolationException_shouldReturn400WithFieldErrors() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("fieldName");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be null");
        when(violation.getInvalidValue()).thenReturn(null);

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<BaseResponse<Void>> response = handler.handleConstraintViolationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("COM_001", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getErrors());
        assertEquals(1, response.getBody().getErrors().size());
    }

    @Test
    @DisplayName("HttpMessageNotReadableException should return 400 with COM_003")
    void handleHttpMessageNotReadableException_shouldReturn400() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Invalid JSON");

        ResponseEntity<BaseResponse<Void>> response = handler.handleHttpMessageNotReadableException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("COM_003", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Unexpected Exception should return 500 with COM_002")
    void handleException_shouldReturn500() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<BaseResponse<Void>> response = handler.handleException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("COM_002", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("NotFoundException with custom message should return 404")
    void handleBaseException_notFoundWithCustomMessage_shouldReturn404() {
        NotFoundException ex = new NotFoundException("QUE_001", "Question not found with id: 123");

        ResponseEntity<BaseResponse<Void>> response = handler.handleBaseException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("QUE_001", response.getBody().getErrorCode());
    }

    @Test
    @DisplayName("DuplicateException for Topic should return 409 with TOP_003")
    void handleBaseException_duplicateTopic_shouldReturn409() {
        DuplicateException ex = new DuplicateException(ErrorCode.TOP_003);

        ResponseEntity<BaseResponse<Void>> response = handler.handleBaseException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TOP_003", response.getBody().getErrorCode());
    }
}
