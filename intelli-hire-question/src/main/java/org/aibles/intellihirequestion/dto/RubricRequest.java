package org.aibles.intellihirequestion.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RubricRequest {

    @NotBlank(message = "Tiêu chí không được để trống")
    @Size(max = 5000, message = "Tiêu chí không được vượt quá 5000 ký tự")
    private String criteria;

    private List<String> keywords;

    @Size(max = 10000, message = "Câu trả lời mẫu không được vượt quá 10000 ký tự")
    private String exampleAnswer;

    private List<String> commonMistakes;

    private List<String> hints;

    @NotNull(message = "Điểm tối đa không được để trống")
    @Min(value = 1, message = "Điểm tối đa phải lớn hơn hoặc bằng 1")
    @Max(value = 100, message = "Điểm tối đa không được vượt quá 100")
    private Integer maxScore = 10;
}
