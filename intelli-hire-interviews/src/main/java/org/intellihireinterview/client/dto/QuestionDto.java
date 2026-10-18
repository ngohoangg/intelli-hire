package org.aibles.intellihireinterview.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionDto {
    private String id;

    @JsonProperty("question_text")
    private String questionText;

    @JsonProperty("topic_id")
    private String topicId;

    @JsonProperty("difficulty_id")
    private String difficultyId;
}
