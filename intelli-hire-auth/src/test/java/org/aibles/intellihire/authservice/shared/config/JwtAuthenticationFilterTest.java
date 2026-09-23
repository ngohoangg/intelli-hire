package org.aibles.intellihire.authservice.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aibles.intellihire.authservice.auth.domain.InvalidTokenException;
import org.aibles.intellihire.authservice.auth.token.AccessTokenClaims;
import org.aibles.intellihire.authservice.auth.token.JwtTokenService;
import org.aibles.intellihire.authservice.shared.service.SessionActivityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtTokenService jwtTokenService;
    @Mock private SessionActivityService sessionActivityService;

    private JwtAuthenticationFilter filter;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenService, sessionActivityService, objectMapper);
        SecurityContextHolder.clearContext();
    }

    private AccessTokenClaims claims() {
        return new AccessTokenClaims(1L, "test@example.com", "testuser", List.of("USER"), List.of());
    }

    @Test
    void activeSession_setsAuthenticationAndContinuesChain() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtTokenService.parseAndValidateAccessToken("valid-token")).thenReturn(claims());
        when(sessionActivityService.isActive(1L)).thenReturn(true);

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(chain.getRequest()).isNotNull(); // chain was invoked
        verify(sessionActivityService).refresh(1L);
    }

    @Test
    void inactiveSession_returns401AndAbortsChain() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtTokenService.parseAndValidateAccessToken("valid-token")).thenReturn(claims());
        when(sessionActivityService.isActive(1L)).thenReturn(false);

        filter.doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(res.getContentType()).contains("application/json");
        assertThat(res.getContentAsString()).contains("AUTH_1009");
        assertThat(chain.getRequest()).isNull(); // chain was NOT invoked
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void noAuthHeader_continuesChainUnauthenticated() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(chain.getRequest()).isNotNull();
        verifyNoInteractions(sessionActivityService);
    }

    @Test
    void invalidToken_continuesChainUnauthenticated() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtTokenService.parseAndValidateAccessToken("bad-token"))
                .thenThrow(new InvalidTokenException("bad"));

        filter.doFilter(req, res, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(sessionActivityService);
    }
}
