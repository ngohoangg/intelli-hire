package org.aibles.intellihirereport.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Report errors
    RPT_001("RPT_001", "Report not found"),
    RPT_002("RPT_002", "Report already exists for session"),
    RPT_003("RPT_003", "Invalid session id"),
    RPT_004("RPT_004", "Invalid report status"),
    RPT_005("RPT_005", "Access denied, admin role required"),
    RPT_006("RPT_006", "Invalid roadmap JSON format"),
    RPT_007("RPT_007", "Access denied, you can only access your own data"),

    // Skill score errors
    SKL_001("SKL_001", "Skill score not found"),

    // User progress errors
    PRG_001("PRG_001", "User progress not found"),

    // Common errors
    COM_001("COM_001", "Validation error"),
    COM_002("COM_002", "Internal server error"),
    COM_003("COM_003", "Bad request");

    private final String code;
    private final String message;
}
