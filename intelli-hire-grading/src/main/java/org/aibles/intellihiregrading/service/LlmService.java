package org.aibles.intellihiregrading.service;

import org.aibles.intellihiregrading.dto.openrouter.LlmGradingResult;

public interface LlmService {
    LlmGradingResult grade(String prompt);
}
