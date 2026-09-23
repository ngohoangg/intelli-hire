package org.aibles.intellihiregrading.dto.interview;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class InterviewBaseResponse {
    private boolean success;
    private String errorCode;
    private InterviewSessionDetail data;
}
