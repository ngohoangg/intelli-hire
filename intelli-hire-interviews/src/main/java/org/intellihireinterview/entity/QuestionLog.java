package org.aibles.intellihireinterview.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "question_logs")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class QuestionLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @Column(name = "question_id", nullable = false, length = 255)
    private String questionId;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    @Column(name = "asked_at")
    private LocalDateTime askedAt;

    @PrePersist
    void prePersist() { if (askedAt == null) askedAt = LocalDateTime.now(); }
}
