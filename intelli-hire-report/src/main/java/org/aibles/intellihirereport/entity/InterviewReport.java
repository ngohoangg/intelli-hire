package org.aibles.intellihirereport.entity;

import jakarta.persistence.*;
import lombok.*;
import org.aibles.intellihirereport.entity.enums.ReportStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "interview_reports")
public class InterviewReport extends BaseEntity {

    @Column(name = "session_id", nullable = false, unique = true, updatable = false)
    private String sessionId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "topic")
    private String topic;

    @Column(name = "total_score")
    private Double totalScore;

    @Column(name = "max_score")
    private Double maxScore;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "roadmap_json", columnDefinition = "jsonb")
    private String roadmapJson;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ReportStatus status = ReportStatus.COMPLETED;
}
