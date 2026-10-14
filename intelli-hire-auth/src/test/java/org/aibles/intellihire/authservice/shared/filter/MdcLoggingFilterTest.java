package org.aibles.intellihire.authservice.shared.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MdcLoggingFilterTest {

    private final MdcLoggingFilter filter = new MdcLoggingFilter();

    @Test
    @DisplayName("Should generate requestId and set in response header and clear MDC after request")
    void shouldGenerateRequestIdWhenNotProvided() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        String headerRequestId = response.getHeader(MdcLoggingFilter.REQUEST_ID_HEADER);
        assertThat(headerRequestId).isNotBlank();
        // MDC must be cleared in finally block
        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    @DisplayName("Should preserve existing requestId and propagate userId to MDC during execution")
    void shouldPreserveExistingHeaders() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(MdcLoggingFilter.REQUEST_ID_HEADER, "req-12345");
        request.addHeader(MdcLoggingFilter.USER_ID_HEADER, "user-999");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, (req, res) -> {
            assertThat(MDC.get("requestId")).isEqualTo("req-12345");
            assertThat(MDC.get("userId")).isEqualTo("user-999");
        });

        assertThat(response.getHeader(MdcLoggingFilter.REQUEST_ID_HEADER)).isEqualTo("req-12345");
        assertThat(MDC.get("requestId")).isNull();
        assertThat(MDC.get("userId")).isNull();
    }
}
