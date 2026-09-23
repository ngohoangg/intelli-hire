package org.aibles.intellihirequestion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "rubrics")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Rubric extends BaseEntity {

    @Column(name = "question_id", nullable = false, unique = true)
    private String questionId;

    @Column(name = "criteria")
    private String criteria;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "keywords", columnDefinition = "jsonb")
    private Map<String, Object> keywords;

    @Column(name = "example_answer")
    private String exampleAnswer;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "common_mistakes", columnDefinition = "jsonb")
    private Map<String, Object> commonMistakes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hints", columnDefinition = "jsonb")
    private Map<String, Object> hints;

    @Column(name = "max_score")
    private Integer maxScore = 10;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
