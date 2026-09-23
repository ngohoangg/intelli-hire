package org.aibles.intellihirequestion.entity.key;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionTagId implements Serializable {
    private String questionId;
    private String tagId;
}
