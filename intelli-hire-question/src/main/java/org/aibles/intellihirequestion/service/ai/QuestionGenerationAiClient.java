package org.aibles.intellihirequestion.service.ai;

import org.aibles.intellihirequestion.dto.GenerateQuestionsFromResumeRequest;
import org.aibles.intellihirequestion.dto.ai.AiGeneratedQuestionResult;
import org.aibles.intellihirequestion.dto.resume.ResumeAggregateData;
import org.aibles.intellihirequestion.entity.DifficultyLevel;
import org.aibles.intellihirequestion.entity.Topic;
import java.util.List;

public interface QuestionGenerationAiClient {
    /**
     * Generate interview questions based on the provided resume data, topics, and difficulty levels.
     *
     * @param resumeData The aggregated data of the candidate's resume.
     * @param request The request containing parameters for question generation.
     * @param topics The list of topics to focus on when generating questions.
     * @param difficulties The list of difficulty levels to consider when generating questions.
     * @return An AiGeneratedQuestionResult containing the generated questions and related information.
     */
    AiGeneratedQuestionResult generateQuestions(ResumeAggregateData resumeData, GenerateQuestionsFromResumeRequest request, List<Topic> topics, List<DifficultyLevel> difficulties);
}
