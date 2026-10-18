package org.aibles.intellihireinterview.mapper;

import org.aibles.intellihireinterview.entity.InterviewSession;
import org.aibles.intellihireinterview.entity.QuestionLog;
import org.aibles.intellihireinterview.entity.UserAnswerRaw;
import org.aibles.intellihireinterview.dto.SessionDetailResponse;
import org.aibles.intellihireinterview.dto.SessionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface SessionMapper {

    @Mapping(target = "status", expression = "java(session.getStatus().name())")
    SessionResponse toResponse(InterviewSession session);

    List<SessionResponse> toResponseList(List<InterviewSession> sessions);

    @Mapping(target = "status", expression = "java(session.getStatus().name())")
    @Mapping(target = "questions", source = "questionLogs")
    @Mapping(target = "answers", ignore = true)
    SessionDetailResponse toDetailResponse(InterviewSession session);

    @Mapping(target = "questionId",    source = "questionId")
    @Mapping(target = "questionText",  source = "questionText")
    @Mapping(target = "questionOrder", source = "questionOrder")
    SessionDetailResponse.QuestionLogDto toQuestionLogDto(QuestionLog log);

    @Mapping(target = "questionId",  source = "questionId")
    @Mapping(target = "answerText",  source = "answerText")
    @Mapping(target = "submittedAt", source = "submittedAt")
    SessionDetailResponse.AnswerDto toAnswerDto(UserAnswerRaw answer);
}
