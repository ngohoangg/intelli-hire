package org.aibles.intellihirequestion.projection;

import java.time.LocalDateTime;

public interface QuestionDetailProjection {
    String getId();
    String getTopicId();
    String getTopicName();
    String getDifficultyId();
    String getDifficultyName();
    String getQuestionText();
    String getQuestionType();
    Boolean getIsActive();
    String getStatus();
    String getSubmittedBy();
    Long getSubmittedById();
    String getRejectionReason();
    LocalDateTime getCreatedAt();
    LocalDateTime getLastUpdatedAt();
}
