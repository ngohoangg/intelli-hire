package org.aibles.intellihirequestion.dto.resume;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResumeData {
    private String id;
    private String userId;
    private String title;
    private String status;
    private Boolean isActive;
}
