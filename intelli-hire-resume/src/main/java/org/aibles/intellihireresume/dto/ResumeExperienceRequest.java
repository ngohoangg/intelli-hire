package org.aibles.intellihireresume.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResumeExperienceRequest {

    @NotBlank(message = "Công ty không được để trống")
    @Size(max = 255, message = "Công ty không được vượt quá 255 ký tự")
    private String company;

    @NotBlank(message = "Chức danh không được để trống")
    @Size(max = 255, message = "Chức danh không được vượt quá 255 ký tự")
    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String description;

    @Builder.Default
    private Boolean isCurrent = false;
}
