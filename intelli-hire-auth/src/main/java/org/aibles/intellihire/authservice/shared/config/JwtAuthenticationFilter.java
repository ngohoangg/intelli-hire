package org.aibles.intellihire.authservice.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.auth.domain.InvalidTokenException;
import org.aibles.intellihire.authservice.auth.token.AccessTokenClaims;
import org.aibles.intellihire.authservice.auth.token.JwtTokenService;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;
import org.aibles.intellihire.authservice.shared.service.SessionActivityService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtTokenService jwtTokenService;
    private final SessionActivityService sessionActivityService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            AccessTokenClaims claims = jwtTokenService.parseAndValidateAccessToken(token);

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            if (claims.roles() != null) {
                claims.roles().forEach(role -> {
                    String roleAuthority = role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role;
                    authorities.add(new SimpleGrantedAuthority(roleAuthority));
                });
            }

            if (claims.permissions() != null) {
                claims.permissions().forEach(permission ->
                        authorities.add(new SimpleGrantedAuthority(permission))
                );
            }

            JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                    claims.accountId(),
                    claims.email(),
                    claims.username(),
                    authorities
            );

            if (!sessionActivityService.isActive(claims.accountId())) {
                sendSessionInactiveResponse(response);
                return;
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
            sessionActivityService.refresh(claims.accountId());
            log.debug("Authenticated account: {}", claims.accountId());

        } catch (InvalidTokenException e) {
            log.debug("Invalid token: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void sendSessionInactiveResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Map<String, String> body = Map.of(
                "code", ErrorCode.SESSION_INACTIVE.getCode(),
                "message", ErrorCode.SESSION_INACTIVE.getMessage()
        );
        objectMapper.writeValue(response.getWriter(), body);
    }
}
