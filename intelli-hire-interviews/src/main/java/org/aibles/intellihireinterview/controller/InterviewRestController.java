package org.aibles.intellihireinterview.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aibles.intellihireinterview.dto.BaseResponse;
import org.aibles.intellihireinterview.dto.CreateSessionRequest;
import org.aibles.intellihireinterview.dto.SessionDetailResponse;
import org.aibles.intellihireinterview.dto.SessionResponse;
import org.aibles.intellihireinterview.dto.SubmitAnswerRequest;
import org.aibles.intellihireinterview.mapper.SessionMapper;
import org.aibles.intellihireinterview.service.AnswerService;
import org.aibles.intellihireinterview.service.InterviewSessionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/interviews/sessions")
@RequiredArgsConstructor
public class InterviewRestController {

    private final InterviewSessionService sessionService;
    private final AnswerService answerService;
    private final SessionMapper sessionMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<SessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request,
                                                       HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        var session = sessionService.createSession(userId, request);
        return BaseResponse.success(sessionMapper.toResponse(session));
    }

    @GetMapping
    public BaseResponse<Page<SessionResponse>> listSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        return BaseResponse.success(sessionService.listSessions(userId, pageable).map(sessionMapper::toResponse));
    }

    @GetMapping("/{sessionId}")
    public BaseResponse<SessionDetailResponse> getSession(@PathVariable Long sessionId,
                                                          HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        return BaseResponse.success(sessionService.getSessionDetail(sessionId, userId));
    }

    @PutMapping("/{sessionId}/end")
    public BaseResponse<Void> endSession(@PathVariable Long sessionId, HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        sessionService.endSession(sessionId, userId);
        return BaseResponse.success(null);
    }

    @DeleteMapping("/{sessionId}")
    public BaseResponse<Void> deleteSession(@PathVariable Long sessionId, HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        sessionService.deleteSession(sessionId, userId);
        return BaseResponse.success(null);
    }

    @PostMapping("/{sessionId}/answers")
    public BaseResponse<Void> submitAnswer(@PathVariable Long sessionId,
                                           @Valid @RequestBody SubmitAnswerRequest request,
                                           HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        answerService.submitAnswer(sessionId, userId, request);
        return BaseResponse.success(null);
    }
}
