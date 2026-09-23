package org.aibles.intellihirequestion.dto.resume;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeAggregateDataDeserializationTest {

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .build();

    @Test
    @DisplayName("Should deserialize snake_case candidate profile payload from resume-service")
    void shouldDeserializeSnakeCaseCandidateProfilePayload() throws Exception {
        String payload = """
                {
                  "success": true,
                  "error_code": null,
                  "data": {
                    "resume_id": "resume-123",
                    "contact": {
                      "full_name": "Nguyen Van A",
                      "email": "a@example.com"
                    },
                    "experiences": [],
                    "educations": [],
                    "skills": [
                      {
                        "id": "resume-skill-1",
                        "skill_id": "skill-java",
                        "skill_name": "Java",
                        "years_experience": 3.5,
                        "confidence_score": 0.92,
                        "is_primary": true
                      }
                    ],
                    "skill_profile": {
                      "years_estimated": 4.0,
                      "seniority": "MID",
                      "summary": "Backend-focused Java engineer."
                    }
                  },
                  "errors": [],
                  "timestamp": "2026-03-28T16:00:00"
                }
                """;

        ResumeApiResponse<ResumeAggregateData> response = objectMapper.readValue(
                payload,
                new TypeReference<>() {}
        );

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getResumeId()).isEqualTo("resume-123");
        assertThat(response.getData().getContact()).isNotNull();
        assertThat(response.getData().getContact().getFullName()).isEqualTo("Nguyen Van A");
        assertThat(response.getData().getSkills()).hasSize(1);
        assertThat(response.getData().getSkills().get(0).getSkillId()).isEqualTo("skill-java");
        assertThat(response.getData().getSkillProfile()).isNotNull();
        assertThat(response.getData().getSkillProfile().getSeniority()).isEqualTo("MID");
    }
}
