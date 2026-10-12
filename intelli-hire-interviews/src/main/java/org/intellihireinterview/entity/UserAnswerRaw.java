package org.aibles.intellihireinterview.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_answers_raw")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserAnswerRaw {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @Column(name = "question_id", nullable = false, length = 255)
    private String questionId;

    @Column(name = "answer_text", nullable = false, columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @PrePersist
    void prePersist() { if (submittedAt == null) submittedAt = LocalDateTime.now(); }
}
