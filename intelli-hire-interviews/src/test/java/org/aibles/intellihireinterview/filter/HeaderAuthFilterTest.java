package org.aibles.intellihireinterview.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class HeaderAuthFilterTest {

    HeaderAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new HeaderAuthFilter(new ObjectMapper().registerModule(new JavaTimeModule()));
    }

    @Test
    void setsUserIdAttribute_whenHeaderPresent() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "42");
        request.addHeader("X-User-Role", "ROLE_USER");
        var response = new MockHttpServletResponse();
        var chainInvoked = new AtomicBoolean(false);

        filter.doFilterInternal(request, response, (req, res) -> {
            chainInvoked.set(true);
            assertThat(req.getAttribute("userId")).isEqualTo("42");
            assertThat(req.getAttribute("userRole")).isEqualTo("ROLE_USER");
        });

        assertThat(chainInvoked.get()).isTrue();
    }

    @Test
    void returns401_whenUserIdHeaderMissing() throws Exception {
        var request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/interviews/sessions");
        var response = new MockHttpServletResponse();
        var chainInvoked = new AtomicBoolean(false);

        filter.doFilterInternal(request, response, (req, res) -> chainInvoked.set(true));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(chainInvoked.get()).isFalse();
    }

    @Test
    void defaultsUserRole_whenRoleHeaderMissing() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "10");
        var response = new MockHttpServletResponse();
        var chainInvoked = new AtomicBoolean(false);

        filter.doFilterInternal(request, response, (req, res) -> {
            chainInvoked.set(true);
            assertThat(req.getAttribute("userRole")).isEqualTo("ROLE_USER");
        });

        assertThat(chainInvoked.get()).isTrue();
    }

    @Test
    void skipsFilter_forActuatorPath() throws Exception {
        var request = new MockHttpServletRequest();
        request.setRequestURI("/actuator/health");
        var response = new MockHttpServletResponse();

        boolean shouldSkip = filter.shouldNotFilter(request);

        assertThat(shouldSkip).isTrue();
    }
}
