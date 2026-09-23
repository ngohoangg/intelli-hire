package org.aibles.intellihiregrading.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Grading errors
    GRD_001("GRD_001", "Grading session not found"),
    GRD_002("GRD_002", "Grading already exists for session"),
    GRD_003("GRD_003", "Invalid session id"),
    GRD_004("GRD_004", "Invalid grading status"),
    GRD_005("GRD_005", "Access denied, admin role required"),
    GRD_006("GRD_006", "LLM service unavailable"),
    GRD_007("GRD_007", "LLM response parse error"),
    GRD_008("GRD_008", "Report service unavailable"),

    // Common errors
    COM_001("COM_001", "Validation error"),
    COM_002("COM_002", "Internal server error"),
    COM_003("COM_003", "Bad request");

    private final String code;
    private final String message;
}
