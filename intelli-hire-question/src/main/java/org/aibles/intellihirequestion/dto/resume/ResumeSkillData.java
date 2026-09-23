package org.aibles.intellihirequestion.dto.resume;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResumeSkillData {
    private String id;
    private String skillId;
    private String skillName;
    private String skillCategory;
    private String skillType;
    private String proficiencyLevel;
    private BigDecimal yearsExperience;
    private BigDecimal confidenceScore;
    private Boolean isPrimary;
}
