package org.aibles.intellihirequestion.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.aibles.intellihirequestion.entity.enums.QuestionType;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class QuestionRequest {

    @NotBlank(message = "Topic ID must not be blank")
    private String topicId;

    @NotBlank(message = "Difficulty ID must not be blank")
    private String difficultyId;

    @NotBlank(message = "Question text must not be blank")
    @Size(max = 5000, message = "Question text must not exceed 5000 characters")
    private String questionText;

    @NotNull(message = "Question type must not be blank")
    private QuestionType questionType;

    private List<String> tagIds;

    private Boolean isActive;
}