package org.aibles.intellihireinterview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.aibles.intellihireinterview.dto.CreateSessionRequest;
import org.aibles.intellihireinterview.dto.SessionDetailResponse;
import org.aibles.intellihireinterview.dto.SessionResponse;
import org.aibles.intellihireinterview.entity.InterviewSession;
import org.aibles.intellihireinterview.entity.enums.SessionStatus;
import org.aibles.intellihireinterview.exception.GlobalExceptionHandler;
import org.aibles.intellihireinterview.exception.SessionNotFoundException;
import org.aibles.intellihireinterview.mapper.SessionMapper;
import org.aibles.intellihireinterview.service.AnswerService;
import org.aibles.intellihireinterview.service.InterviewSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InterviewRestController.class)
@Import(GlobalExceptionHandler.class)
class InterviewRestControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean InterviewSessionService sessionService;
    @MockBean AnswerService answerService;
    @MockBean SessionMapper sessionMapper;

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void createSession_returns201() throws Exception {
        var session = InterviewSession.builder()
            .id(1L).userId("42").topic("Java").status(SessionStatus.ACTIVE).build();
        var response = new SessionResponse();
        response.setId(1L);
        response.setTopic("Java");
        response.setStatus("ACTIVE");

        when(sessionService.createSession(eq("42"), any())).thenReturn(session);
        when(sessionMapper.toResponse(session)).thenReturn(response);

        var request = new CreateSessionRequest();
        request.setTopic("Java");
        request.setDifficulty("mid");

        mockMvc.perform(post("/api/v1/interviews/sessions")
                .header("X-User-Id", "42")
                .requestAttr("userId", "42")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.topic").value("Java"))
            .andExpect(jsonPath("$.data.status").value("ACTIVE"))
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void listSessions_returns200WithPaginatedResult() throws Exception {
        when(sessionService.listSessions(eq("42"), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/interviews/sessions")
                .header("X-User-Id", "42")
                .requestAttr("userId", "42"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.total_elements").exists())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createSession_withBlankTopic_returns400() throws Exception {
        var request = new CreateSessionRequest();
        request.setTopic("");

        mockMvc.perform(post("/api/v1/interviews/sessions")
                .header("X-User-Id", "42")
                .requestAttr("userId", "42")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getSession_whenNotFound_returns404() throws Exception {
        when(sessionService.getSessionDetail(99L, "42")).thenThrow(new SessionNotFoundException(99L));

        mockMvc.perform(get("/api/v1/interviews/sessions/99")
                .header("X-User-Id", "42")
                .requestAttr("userId", "42"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error_code").value("INT_001"));
    }

    @Test
    void endSession_returns204() throws Exception {
        doNothing().when(sessionService).endSession(1L, "42");

        mockMvc.perform(put("/api/v1/interviews/sessions/1/end")
                .header("X-User-Id", "42")
                .requestAttr("userId", "42"))
            .andExpect(status().isOk());
    }
}
