package org.aibles.intellihirequestion.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiGeneratedQuestion {
    private String questionText;
    private String questionType;
    private String topicName;
    private String difficultyName;
    private String rationale;
}
