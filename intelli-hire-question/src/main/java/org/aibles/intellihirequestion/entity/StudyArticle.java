package org.aibles.intellihirequestion.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "study_articles")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudyArticle extends BaseEntity {

    @Column(name = "topic_id", nullable = false)
    private String topicId;

    @Column(name = "difficulty_id", nullable = false)
    private String difficultyId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "description")
    private String description;

    @Column(name = "source", length = 100)
    private String source;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
