package org.aibles.intellihireresume.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResumeSkillRequest {

    @NotBlank(message = "Skill ID không được để trống")
    private String skillId;

    @Size(max = 20, message = "Mức độ thành thạo không được vượt quá 20 ký tự")
    private String proficiencyLevel;

    @DecimalMin(value = "0.0", message = "Số năm kinh nghiệm phải không âm")
    @DecimalMax(value = "50.0", message = "Số năm kinh nghiệm không được vượt quá 50")
    @Digits(integer = 2, fraction = 1)
    private BigDecimal yearsExperience;

    @DecimalMin(value = "0.0", message = "Điểm tin cậy phải nằm trong khoảng từ 0.0 đến 1.0")
    @DecimalMax(value = "1.0", message = "Điểm tin cậy phải nằm trong khoảng từ 0.0 đến 1.0")
    @Digits(integer = 1, fraction = 2)
    private BigDecimal confidenceScore;

    @Size(max = 500, message = "Nội dung minh chứng không được vượt quá 500 ký tự")
    private String evidenceText;

    @Builder.Default
    private Boolean isPrimary = false;
}
