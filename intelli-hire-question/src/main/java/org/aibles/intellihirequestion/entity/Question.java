package org.aibles.intellihirequestion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.aibles.intellihirequestion.entity.converter.QuestionTypeConverter;
import org.aibles.intellihirequestion.entity.enums.QuestionType;

@Entity
@Table(name = "questions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Question extends BaseEntity {

    @Column(name = "topic_id", nullable = false)
    private String topicId;

    @Column(name = "difficulty_id", nullable = false)
    private String difficultyId;

    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Convert(converter = QuestionTypeConverter.class)
    @Column(name = "question_type", nullable = false, length = 50)
    private QuestionType questionType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "submitted_by", length = 255)
    private String submittedBy;

    @Column(name = "submitted_by_id")
    private Long submittedById;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
}