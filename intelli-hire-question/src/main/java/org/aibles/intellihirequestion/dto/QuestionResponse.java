package org.aibles.intellihirequestion.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class QuestionResponse {
    private String id;
    private String topicId;
    private String topicName;
    private String difficultyId;
    private String difficultyName;
    private String questionText;
    private String questionType;
    private List<TagResponse> tags;
    private Boolean isActive;
    private String status;
    private String submittedBy;
    private Long submittedById;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
}