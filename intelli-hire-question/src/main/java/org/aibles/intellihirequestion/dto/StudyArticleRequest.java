package org.aibles.intellihirequestion.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StudyArticleRequest {

    @NotBlank(message = "Topic ID must not be blank")
    private String topicId;

    @NotBlank(message = "Difficulty ID must not be blank")
    private String difficultyId;

    @NotBlank(message = "Title must not be blank")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "URL must not be blank")
    @Size(max = 500, message = "URL must not exceed 500 characters")
    private String url;

    private String description;

    @Size(max = 100, message = "Source must not exceed 100 characters")
    private String source;

    @Builder.Default
    private Boolean isActive = true;
}
