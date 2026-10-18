package org.aibles.intellihireinterview.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SessionDetailResponse {
    private Long id;
    private String topic;
    private String difficulty;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private List<QuestionLogDto> questions;
    private List<AnswerDto> answers;

    @Data
    public static class QuestionLogDto {
        private String questionId;
        private String questionText;
        private Integer questionOrder;
    }

    @Data
    public static class AnswerDto {
        private String questionId;
        private String answerText;
        private LocalDateTime submittedAt;
    }
}
