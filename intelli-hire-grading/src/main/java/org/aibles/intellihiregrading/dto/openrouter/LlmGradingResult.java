package org.aibles.intellihiregrading.dto.openrouter;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmGradingResult {
    private Double score;
    private String feedback;
    private String model;
}
