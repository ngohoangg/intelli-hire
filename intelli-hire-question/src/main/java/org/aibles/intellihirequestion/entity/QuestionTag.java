package org.aibles.intellihirequestion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.aibles.intellihirequestion.entity.key.QuestionTagId;

@Entity
@Table(name = "question_tags")
@IdClass(QuestionTagId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionTag {

    @Id
    @Column(name = "question_id")
    private String questionId;

    @Id
    @Column(name = "tag_id")
    private String tagId;
}
