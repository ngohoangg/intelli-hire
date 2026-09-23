package org.aibles.intellihirequestion.dto.resume;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResumeContactData {
    private String fullName;
    private String email;
    private String phone;
    private String location;
    private String linkedinUrl;
    private String otherInfo;
}
