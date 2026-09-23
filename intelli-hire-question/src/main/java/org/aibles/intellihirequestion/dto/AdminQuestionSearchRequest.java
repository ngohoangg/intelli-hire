package org.aibles.intellihirequestion.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminQuestionSearchRequest {

    private String status;
    private String topicId;
    private String type;
    private String keyword;
    private String submittedBy;
}
