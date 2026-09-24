package org.aibles.intellihireresume.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResumeEducationRequest {

    @NotBlank(message = "Trường học không được để trống")
    @Size(max = 255, message = "Trường học không được vượt quá 255 ký tự")
    private String school;

    @Size(max = 255, message = "Bằng cấp không được vượt quá 255 ký tự")
    private String degree;

    @Size(max = 255, message = "Ngành học không được vượt quá 255 ký tự")
    private String field;

    private Integer startYear;
    private Integer endYear;
    private String description;
}
