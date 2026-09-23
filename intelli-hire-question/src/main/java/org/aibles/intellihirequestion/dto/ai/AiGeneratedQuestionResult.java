package org.aibles.intellihirequestion.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiGeneratedQuestionResult {
    private String candidateSummary;
    private List<AiGeneratedQuestion> questions;
}
