package org.aibles.intellihireinterview.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Interview session errors
    INT_001("INT_001", "Interview session not found"),
    INT_002("INT_002", "Interview session is not active"),
    INT_003("INT_003", "Answer already submitted for this question"),
    INT_004("INT_004", "No questions available for this topic. Please try a different topic or difficulty."),

    // Common errors
    COM_001("COM_001", "Validation error"),
    COM_002("COM_002", "Internal server error"),
    COM_003("COM_003", "Bad request"),
    COM_004("COM_004", "Missing required header: X-User-Id");

    private final String code;
    private final String message;
}
