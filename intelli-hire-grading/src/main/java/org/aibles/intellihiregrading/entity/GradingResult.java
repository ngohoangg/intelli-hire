package org.aibles.intellihiregrading.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "grading_results")
public class GradingResult extends BaseEntity {

    @Column(name = "grading_session_id", nullable = false)
    private String gradingSessionId;

    @Column(name = "question_id", nullable = false)
    private String questionId;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "skill_name")
    private String skillName;

    @Column(name = "score")
    @Builder.Default
    private Double score = 0.0;

    @Column(name = "max_score")
    @Builder.Default
    private Double maxScore = 10.0;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "prompt_used", columnDefinition = "TEXT")
    private String promptUsed;

    @Column(name = "model_used")
    private String modelUsed;

    @Column(name = "latency_ms")
    private Long latencyMs;
}
