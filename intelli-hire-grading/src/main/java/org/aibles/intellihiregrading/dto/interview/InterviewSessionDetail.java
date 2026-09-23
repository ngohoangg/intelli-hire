package org.aibles.intellihiregrading.dto.interview;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class InterviewSessionDetail {
    private Long id;
    private String topic;
    private String difficulty;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private List<QuestionLogDto> questions;
    private List<AnswerDto> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class QuestionLogDto {
        private String questionId;
        private String questionText;
        private Integer questionOrder;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AnswerDto {
        private String questionId;
        private String answerText;
        private LocalDateTime submittedAt;
    }
}
