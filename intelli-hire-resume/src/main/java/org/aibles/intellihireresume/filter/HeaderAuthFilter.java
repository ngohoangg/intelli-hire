package org.aibles.intellihireresume.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.BaseResponse;
import org.aibles.intellihireresume.dto.FieldError;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class HeaderAuthFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");

        // TODO: temporarily inject default user for local dev without auth service
        if (userId == null || userId.isBlank()) {
            userId = "user-001";
            HttpServletRequest wrappedRequest = new jakarta.servlet.http.HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    if ("X-User-Id".equalsIgnoreCase(name)) return "user-001";
                    if ("X-User-Role".equalsIgnoreCase(name)) return "ROLE_USER";
                    return super.getHeader(name);
                }
                @Override
                public Enumeration<String> getHeaders(String name) {
                    if ("X-User-Id".equalsIgnoreCase(name)) return Collections.enumeration(List.of("user-001"));
                    if ("X-User-Role".equalsIgnoreCase(name)) return Collections.enumeration(List.of("ROLE_USER"));
                    return super.getHeaders(name);
                }
                @Override
                public Enumeration<String> getHeaderNames() {
                    List<String> names = new ArrayList<>(Collections.list(super.getHeaderNames()));
                    if (!names.contains("X-User-Id")) names.add("X-User-Id");
                    if (!names.contains("X-User-Role")) names.add("X-User-Role");
                    return Collections.enumeration(names);
                }
            };
            wrappedRequest.setAttribute("userId", userId);
            wrappedRequest.setAttribute("userRole", "ROLE_USER");
            filterChain.doFilter(wrappedRequest, response);
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
        response.setStatus(HttpStatus.FORBIDDEN.value());
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
