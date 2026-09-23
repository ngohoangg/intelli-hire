package org.aibles.intellihirereport.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "question_scores")
public class QuestionScore extends BaseEntity {

    @Column(name = "report_id", nullable = false)
    private String reportId;

    @Column(name = "question_id", nullable = false)
    private String questionId;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "score")
    private Double score;

    @Column(name = "max_score")
    @Builder.Default
    private Double maxScore = 10.0;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "skill_name")
    private String skillName;
}
