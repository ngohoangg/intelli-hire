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

    @NotBlank(message = "Mã chủ đề không được để trống")
    private String topicId;

    @NotBlank(message = "Mã độ khó không được để trống")
    private String difficultyId;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;

    @NotBlank(message = "URL không được để trống")
    @Size(max = 500, message = "URL không được vượt quá 500 ký tự")
    private String url;

    private String description;

    @Size(max = 100, message = "Nguồn không được vượt quá 100 ký tự")
    private String source;

    @Builder.Default
    private Boolean isActive = true;
}
