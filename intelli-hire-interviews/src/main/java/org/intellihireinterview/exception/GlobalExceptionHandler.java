package org.aibles.intellihireinterview.exception;

import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireinterview.dto.BaseResponse;
import org.aibles.intellihireinterview.dto.FieldError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Void>> handleBaseException(BaseException ex) {
        var body = BaseResponse.<Void>error(
                ex.getErrorCode(),
                List.of(FieldError.builder()
                        .field("error")
                        .message(ex.getMessage())
                        .build())
        );
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BaseResponse<Void>> handleIllegalState(IllegalStateException ex) {
        var body = BaseResponse.<Void>error(
                ErrorCode.COM_003.getCode(),
                List.of(FieldError.builder()
                        .field("error")
                        .message(ex.getMessage())
                        .build())
        );
        return ResponseEntity.status(409).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> FieldError.builder()
                        .field(e.getField())
                        .message(e.getDefaultMessage())
                        .rejectedValue(e.getRejectedValue())
                        .build())
                .toList();
        var body = BaseResponse.<Void>error(ErrorCode.COM_001.getCode(), errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGeneral(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        var body = BaseResponse.<Void>error(
                ErrorCode.COM_002.getCode(),
                List.of(FieldError.builder()
                        .field("error")
                        .message("Internal server error")
                        .build())
        );
        return ResponseEntity.status(500).body(body);
    }
}
