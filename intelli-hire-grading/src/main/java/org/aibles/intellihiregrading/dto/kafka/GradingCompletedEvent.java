package org.aibles.intellihiregrading.dto.kafka;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingCompletedEvent {
    private String sessionId;
    private String userId;
    private String topic;
    private List<QuestionScoreEvent> questionScores;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionScoreEvent {
        private String questionId;
        private String questionText;
        private String answerText;
        private Double score;
        private Double maxScore;
        private String feedback;
        private String skillName;
    }
}
