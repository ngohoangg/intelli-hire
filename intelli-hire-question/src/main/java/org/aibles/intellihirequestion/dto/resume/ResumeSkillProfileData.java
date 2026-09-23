package org.aibles.intellihirequestion.dto.resume;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResumeSkillProfileData {
    private Map<String, Object> topSkills;
    private BigDecimal yearsEstimated;
    private String seniority;
    private String summary;
    private Map<String, Object> signals;
}
