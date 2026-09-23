package org.aibles.intellihirereport.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_progress")
public class UserProgress extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "skill_name", nullable = false)
    private String skillName;

    @Column(name = "latest_score")
    private Double latestScore;

    @Column(name = "average_score")
    private Double averageScore;

    @Column(name = "total_attempts")
    @Builder.Default
    private Integer totalAttempts = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "score_trend", columnDefinition = "jsonb")
    private List<Double> scoreTrend;
}
