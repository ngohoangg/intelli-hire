package org.aibles.intellihireinterview.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireinterview.dto.BaseResponse;
import org.aibles.intellihireinterview.dto.FieldError;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class HeaderAuthFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // TODO: temporarily disabled for local dev without auth service
        return true;
        // String path = request.getRequestURI();
        // return path.startsWith("/actuator") || path.startsWith("/ws/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String userId = request.getHeader("X-User-Id");

        if (userId == null || userId.isBlank()) {
            log.warn("Request rejected: missing X-User-Id header for path={}", request.getRequestURI());
            sendForbidden(response, "Thiếu tiêu đề bắt buộc: X-User-Id");
            return;
        }

        String userRole = request.getHeader("X-User-Role");
        if (userRole == null || userRole.isBlank()) {
            userRole = "ROLE_USER";
        }

        request.setAttribute("userId", userId);
        request.setAttribute("userRole", userRole);

        filterChain.doFilter(request, response);
    }

    private void sendForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        List<FieldError> errors = List.of(
                FieldError.builder()
                        .field("X-User-Id")
                        .message(message)
                        .rejectedValue(null)
                        .build()
        );
        BaseResponse<Void> body = BaseResponse.error("COM_004", errors);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
