package org.aibles.intellihirequestion.service;

import org.aibles.intellihirequestion.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuestionService {

    QuestionResponse create(QuestionRequest request, String userId, String username);

    void delete(String id);

    QuestionResponse update(String id, QuestionRequest request, String updatedBy);

    QuestionResponse getById(String id);

    List<QuestionResponse> getRandomQuestions(RandomQuestionRequest request);

    GenerateQuestionsFromResumeResponse generateFromResume(String userId, GenerateQuestionsFromResumeRequest request);

    long countQuestions(String topicId, String difficultyId);

    PageResponse<QuestionResponse> searchQuestion(QuestionSearchRequest request, Pageable pageable);

    PageResponse<QuestionResponse> getByTopicId(String topicId, Pageable pageable);

    BulkImportResponse bulkImport(BulkImportRequest request);

    // Admin
    PageResponse<QuestionResponse> adminListQuestions(AdminQuestionSearchRequest request, Pageable pageable);

    QuestionResponse adminGetById(String id);

    QuestionResponse adminPatchModerationStatus(String id, String status, String rejectionReason);

    // User submission
    PageResponse<QuestionResponse> getSubmittedQuestions(Long userId, Pageable pageable);

    QuestionResponse resubmit(String id, Long userId);
}
