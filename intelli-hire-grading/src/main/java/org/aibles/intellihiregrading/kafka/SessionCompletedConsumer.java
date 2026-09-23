package org.aibles.intellihiregrading.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihiregrading.dto.AnswerRequest;
import org.aibles.intellihiregrading.dto.GradeRequest;
import org.aibles.intellihiregrading.dto.GradingResponse;
import org.aibles.intellihiregrading.dto.interview.InterviewSessionDetail;
import org.aibles.intellihiregrading.dto.kafka.SessionCompletedEvent;
import org.aibles.intellihiregrading.service.GradingService;
import org.aibles.intellihiregrading.service.InterviewClientService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionCompletedConsumer {

    private final InterviewClientService interviewClientService;
    private final GradingService gradingService;

    @KafkaListener(
            topics = "${kafka.topics.session-completed}",
            groupId = "grading-service",
            containerFactory = "sessionCompletedListenerFactory"
    )
    public void onSessionCompleted(SessionCompletedEvent event) {
        log.info("Received session_completed event: sessionId={}, userId={}, topic={}",
                event.getSessionId(), event.getUserId(), event.getTopic());

        // 1. Fetch full session detail from Interview Service
        InterviewSessionDetail detail = interviewClientService
                .getSessionDetail(event.getSessionId(), event.getUserId())
                .orElseThrow(() -> new RuntimeException(
                        "Could not fetch session detail from Interview Service: sessionId=" + event.getSessionId()));

        if (detail.getAnswers() == null || detail.getAnswers().isEmpty()) {
            log.warn("Session has no answers to grade: sessionId={}", event.getSessionId());
            return;
        }

        // 2. Map questions by questionId for quick lookup
        Map<String, String> questionTextMap = detail.getQuestions() != null
                ? detail.getQuestions().stream()
                    .collect(Collectors.toMap(
                            InterviewSessionDetail.QuestionLogDto::getQuestionId,
                            InterviewSessionDetail.QuestionLogDto::getQuestionText,
                            (a, b) -> a))
                : Map.of();

        // 3. Build GradeRequest from Interview data
        List<AnswerRequest> answers = new ArrayList<>();
        for (InterviewSessionDetail.AnswerDto answer : detail.getAnswers()) {
            String questionText = questionTextMap.getOrDefault(answer.getQuestionId(), "");
            answers.add(AnswerRequest.builder()
                    .questionId(answer.getQuestionId())
                    .questionText(questionText)
                    .answerText(answer.getAnswerText())
                    .skillName(null)
                    .maxScore(10.0)
                    .build());
        }

        GradeRequest gradeRequest = GradeRequest.builder()
                .sessionId(String.valueOf(event.getSessionId()))
                .userId(event.getUserId())
                .topic(event.getTopic())
                .answers(answers)
                .build();

        // 4. Grade — exceptions propagate to Kafka error handler for retry
        GradingResponse response = gradingService.grade(gradeRequest);

        log.info("Kafka grading completed: sessionId={}, status={}, totalScore={}/{}",
                event.getSessionId(),
                response.getStatus(),
                response.getTotalScore(),
                response.getMaxScore());
    }
}
