package org.aibles.intellihire.authservice.shared.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.aibles.intellihire.authservice.shared.config.RateLimitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private InMemoryRateLimiter rateLimiter;

    private RateLimitProperties properties;
    private ObjectMapper objectMapper;
    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        objectMapper = new ObjectMapper().findAndRegisterModules();
        filter = new RateLimitFilter(rateLimiter, properties, objectMapper);
    }

    @Test
    @DisplayName("Should proceed with chain when rate limit allows request")
    void shouldProceedWhenAllowed() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.tryConsume(anyString())).thenReturn(true);

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertThat(chainCalled).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should return 429 Too Many Requests when rate limit is exceeded")
    void shouldReturn429WhenLimitExceeded() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.tryConsume(anyString())).thenReturn(false);
        when(rateLimiter.getRetryAfterSeconds(anyString())).thenReturn(60L);

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertThat(chainCalled).isFalse();
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isEqualTo("60");
        assertThat(response.getContentAsString()).contains("RATE_5001");
    }

    @Test
    @DisplayName("Should bypass rate limiting when disabled in properties")
    void shouldBypassWhenDisabled() throws ServletException, IOException {
        properties.setEnabled(false);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertThat(chainCalled).isTrue();
        verifyNoInteractions(rateLimiter);
    }
}
