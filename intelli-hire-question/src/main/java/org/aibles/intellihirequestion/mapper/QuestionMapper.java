package org.aibles.intellihirequestion.mapper;

import org.aibles.intellihirequestion.dto.QuestionRequest;
import org.aibles.intellihirequestion.dto.QuestionResponse;
import org.aibles.intellihirequestion.dto.TagResponse;
import org.aibles.intellihirequestion.entity.Question;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Mapper class for converting Question entity to QuestionResponse DTO
 */
@Component
public class QuestionMapper {

    /**
     * Converts a QuestionRequest DTO to a Question entity.
     *
     * @param request the QuestionRequest DTO to convert
     * @return the corresponding Question entity
     */
    public Question toEntity(QuestionRequest request) {
        if (request == null) return null;

        return Question.builder()
                .topicId(request.getTopicId())
                .difficultyId(request.getDifficultyId())
                .questionText(request.getQuestionText())
                .questionType(request.getQuestionType())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }

    /**
     * Converts a Question entity to a QuestionResponse DTO.
     *
     * @param question       the Question entity to convert
     * @param topicName      the name of the topic
     * @param difficultyName the name of the difficulty level
     * @param tags           the list of tags associated with the question
     * @return the corresponding QuestionResponse DTO
     */
    public QuestionResponse toResponse(Question question, String topicName, String difficultyName, List<TagResponse> tags) {
        if (question == null) {
            return null;
        }

        return QuestionResponse.builder()
                .id(question.getId())
                .topicId(question.getTopicId())
                .topicName(topicName)
                .difficultyId(question.getDifficultyId())
                .difficultyName(difficultyName)
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType().name())
                .tags(tags)
                .isActive(question.getIsActive())
                .status(question.getStatus())
                .submittedBy(question.getSubmittedBy())
                .submittedById(question.getSubmittedById())
                .rejectionReason(question.getRejectionReason())
                .createdAt(question.getCreatedAt())
                .lastUpdatedAt(question.getLastUpdatedAt())
                .build();
    }
}