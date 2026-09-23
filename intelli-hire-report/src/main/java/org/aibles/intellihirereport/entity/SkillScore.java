package org.aibles.intellihirereport.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "skill_scores")
public class SkillScore extends BaseEntity {

    @Column(name = "report_id", nullable = false)
    private String reportId;

    @Column(name = "skill_name", nullable = false)
    private String skillName;

    @Column(name = "score")
    private Double score;

    @Column(name = "max_score")
    private Double maxScore;

    @Column(name = "question_count")
    private Integer questionCount;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
}
