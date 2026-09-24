package org.aibles.intellihirequestion.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DifficultyLevelRequest {

    @NotBlank(message = "Tên là bắt buộc")
    @Size(max = 50, message = "Tên không được vượt quá 50 ký tự")
    private String name;

    @Min(value = 1, message = "Trọng số phải lớn hơn hoặc bằng 1")
    @Max(value = 100, message = "Trọng số phải nhỏ hơn hoặc bằng 100")
    @NotNull(message = "Trọng số là bắt buộc")
    private Integer weight;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String description;
}
