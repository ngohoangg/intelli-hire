package org.aibles.intellihirequestion.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StudyArticleResponse {
    private String id;
    private String topicId;
    private String topicName;
    private String difficultyId;
    private String difficultyName;
    private String title;
    private String url;
    private String description;
    private String source;
    private Boolean isActive;
}
