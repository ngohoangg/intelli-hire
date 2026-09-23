package org.aibles.intellihirequestion.dto.resume;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResumeAggregateData {
    private String resumeId;
    private ResumeContactData contact;
    private List<ResumeExperienceData> experiences;
    private List<ResumeEducationData> educations;
    private List<ResumeSkillData> skills;
    private ResumeSkillProfileData skillProfile;
}
