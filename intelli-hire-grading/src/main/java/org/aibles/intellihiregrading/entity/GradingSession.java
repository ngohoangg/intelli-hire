package org.aibles.intellihiregrading.entity;

import jakarta.persistence.*;
import lombok.*;
import org.aibles.intellihiregrading.entity.enums.GradingStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "grading_sessions")
public class GradingSession extends BaseEntity {

    @Column(name = "session_id", nullable = false, unique = true, updatable = false)
    private String sessionId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "topic")
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private GradingStatus status = GradingStatus.PROCESSING;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "graded_count")
    @Builder.Default
    private Integer gradedCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
