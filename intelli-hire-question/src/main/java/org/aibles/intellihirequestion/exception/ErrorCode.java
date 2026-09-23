package org.aibles.intellihirequestion.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Question errors
    QUE_001("QUE_001", "Question not found"),
    QUE_002("QUE_002", "Question already exists"),
    QUE_003("QUE_003", "Invalid question type"),

    // Topic errors
    TOP_001("TOP_001", "Topic not found"),
    TOP_002("TOP_002", "Topic circular reference"),
    TOP_003("TOP_003", "Topic already exists"),
    TOP_004("TOP_004", "The topic is locked"),
    TOP_005("TOP_005", "Topic has active children"),
    TOP_006("TOP_006", "Topic has active questions"),

    // Rubric errors
    RUB_001("RUB_001", "Rubric not found"),
    RUB_002("RUB_002", "Rubric already exists for question"),

    // Study article errors
    ART_001("ART_001", "Study article not found"),

    // Tag errors
    TAG_001("TAG_001", "Tag not found"),
    TAG_002("TAG_002", "Tag already exists"),
    TAG_003("TAG_003", "Tag is in use by questions"),
    TAG_004("TAG_004", "The tag is locked"),

    // Difficulty errors
    DIF_001("DIF_001", "Difficulty level not found"),
    DIF_002("DIF_002", "Difficulty level already exists"),
    DIF_003("DIF_003", "Difficulty level is in use by questions or study articles"),

    // Study article errors
    SA_001("SA_001", "Study article not found"),

    // Random question errors
    QUE_004("QUE_004", "Invalid question count, must be between 1 and 50"),
    QUE_005("QUE_005", "Not enough questions available"),

    // Bulk import errors
    QUE_007("QUE_007", "Questions list must not be empty"),
    QUE_008("QUE_008", "Questions list must not exceed 100 items"),
    QUE_009("QUE_009", "Question generation request is invalid"),

    // Resume client errors
    RSM_001("RSM_001", "Resume not found"),

    // AI errors
    AI_001("AI_001", "AI provider authentication failed"),
    AI_002("AI_002", "AI provider request failed"),

    // Common errors
    COM_001("COM_001", "Validation error"),
    COM_002("COM_002", "Internal server error"),
    COM_003("COM_003", "Bad request");

    private final String code;
    private final String message;
}
