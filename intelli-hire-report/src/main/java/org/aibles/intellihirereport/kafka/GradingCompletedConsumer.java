package org.aibles.intellihirereport.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihirereport.dto.CreateReportRequest;
import org.aibles.intellihirereport.dto.QuestionScoreRequest;
import org.aibles.intellihirereport.dto.ReportResponse;
import org.aibles.intellihirereport.dto.kafka.GradingCompletedEvent;
import org.aibles.intellihirereport.service.ReportService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GradingCompletedConsumer {

    private final ReportService reportService;

    @KafkaListener(
            topics = "${kafka.topics.grading-completed}",
            groupId = "report-service",
            containerFactory = "gradingCompletedListenerFactory"
    )
    public void onGradingCompleted(GradingCompletedEvent event) {
        log.info("Received grading_completed event: sessionId={}, userId={}, topic={}",
                event.getSessionId(), event.getUserId(), event.getTopic());

        try {
            List<QuestionScoreRequest> questionScores = event.getQuestionScores().stream()
                    .map(qs -> QuestionScoreRequest.builder()
                            .questionId(qs.getQuestionId())
                            .questionText(qs.getQuestionText())
                            .answerText(qs.getAnswerText())
                            .score(qs.getScore())
                            .maxScore(qs.getMaxScore())
                            .feedback(qs.getFeedback())
                            .skillName(qs.getSkillName())
                            .build())
                    .collect(Collectors.toList());

            CreateReportRequest request = CreateReportRequest.builder()
                    .sessionId(event.getSessionId())
                    .userId(Long.parseLong(event.getUserId()))
                    .topic(event.getTopic())
                    .questionScores(questionScores)
                    .build();

            ReportResponse response = reportService.create(request);

            log.info("Report created from Kafka event: sessionId={}, reportId={}, totalScore={}/{}",
                    event.getSessionId(),
                    response.getId(),
                    response.getTotalScore(),
                    response.getMaxScore());

        } catch (Exception e) {
            log.error("Failed to create report from grading_completed event: sessionId={}, error={}",
                    event.getSessionId(), e.getMessage(), e);
        }
    }
}
