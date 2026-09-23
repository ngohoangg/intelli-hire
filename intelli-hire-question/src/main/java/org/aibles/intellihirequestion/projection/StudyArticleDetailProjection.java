package org.aibles.intellihirequestion.projection;

public interface StudyArticleDetailProjection {
    String getId();
    String getTopicId();
    String getTopicName();
    String getDifficultyId();
    String getDifficultyName();
    String getTitle();
    String getUrl();
    String getDescription();
    String getSource();
    Boolean getIsActive();
}
