package org.aibles.intellihirequestion.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BulkImportRequest {

    @NotEmpty(message = "Danh sách câu hỏi không được để trống")
    @Size(max = 100, message = "Danh sách câu hỏi không được vượt quá 100 mục")
    @Valid
    private List<QuestionRequest> questions;
}
