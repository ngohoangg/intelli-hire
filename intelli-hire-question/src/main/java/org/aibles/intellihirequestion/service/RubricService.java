package org.aibles.intellihirequestion.service;

import org.aibles.intellihirequestion.dto.RubricRequest;
import org.aibles.intellihirequestion.dto.RubricResponse;

public interface RubricService {

    /**
     * Create a rubric for a question.
     *
     * @param questionId the question ID to create rubric for
     * @param request    the rubric request containing rubric details
     * @return the created rubric response
     * @throws org.aibles.intellihirequestion.exception.NotFoundException if question not found
     * @throws org.aibles.intellihirequestion.exception.DuplicateException if rubric already exists for question
     */
    RubricResponse createRubric(String questionId, RubricRequest request);

    /**
     * Get rubric by question ID.
     *
     * @param questionId the question ID to get rubric for
     * @return the rubric response
     * @throws org.aibles.intellihirequestion.exception.NotFoundException if question or rubric not found
     */
    RubricResponse getRubric(String questionId);

    /**
     * Update rubric for a question.
     *
     * @param questionId the question ID
     * @param request    the rubric request containing updated details
     * @return the updated rubric response
     * @throws org.aibles.intellihirequestion.exception.NotFoundException if question or rubric not found
     */
    RubricResponse updateRubric(String questionId, RubricRequest request);
}
