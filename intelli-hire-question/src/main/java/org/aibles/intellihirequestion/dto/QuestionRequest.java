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

    @NotBlank(message = "Mã chủ đề không được để trống")
    private String topicId;

    @NotBlank(message = "Mã độ khó không được để trống")
    private String difficultyId;

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    @Size(max = 5000, message = "Nội dung câu hỏi không được vượt quá 5000 ký tự")
    private String questionText;

    @NotNull(message = "Loại câu hỏi không được để trống")
    private QuestionType questionType;

    private List<String> tagIds;

    private Boolean isActive;
}