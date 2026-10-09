package org.aibles.intellihire.authservice.shared.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JpaAuditingConfigTest {

    private final JpaAuditingConfig config = new JpaAuditingConfig();
    private final AuditorAware<String> auditorAware = config.auditorProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    @DisplayName("Should resolve auditor from SecurityContext when authenticated")
    void shouldResolveFromSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("auth-user-123", "credentials", Collections.emptyList())
        );

        Optional<String> auditor = auditorAware.getCurrentAuditor();
        assertThat(auditor).contains("auth-user-123");
    }

    @Test
    @DisplayName("Should resolve auditor from MDC userId when SecurityContext is empty")
    void shouldResolveFromMdc() {
        MDC.put("userId", "mdc-user-456");

        Optional<String> auditor = auditorAware.getCurrentAuditor();
        assertThat(auditor).contains("mdc-user-456");
    }

    @Test
    @DisplayName("Should fallback to system when neither SecurityContext nor MDC has user")
    void shouldFallbackToSystem() {
        Optional<String> auditor = auditorAware.getCurrentAuditor();
        assertThat(auditor).contains("system");
    }
}
