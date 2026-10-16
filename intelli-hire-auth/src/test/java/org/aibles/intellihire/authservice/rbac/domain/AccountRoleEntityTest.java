package org.aibles.intellihire.authservice.rbac.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AccountRoleEntityTest {

    @Test
    @DisplayName("Should initialize assignedAt with current time and expiresAt as null")
    void shouldInitializeWithDefaultAssignedAt() {
        AccountRoleEntity accountRole = new AccountRoleEntity();
        accountRole.setAccountId(1L);
        accountRole.setRoleId(2L);

        assertThat(accountRole.getAccountId()).isEqualTo(1L);
        assertThat(accountRole.getRoleId()).isEqualTo(2L);
        assertThat(accountRole.getAssignedAt()).isNotNull();
        assertThat(accountRole.getExpiresAt()).isNull();
        assertThat(accountRole.isExpired()).isFalse();
    }

    @Test
    @DisplayName("Should correctly determine whether account role assignment is expired")
    void shouldCheckIfExpired() {
        AccountRoleEntity accountRole = new AccountRoleEntity();

        // No expiry set -> not expired
        accountRole.setExpiresAt(null);
        assertThat(accountRole.isExpired()).isFalse();

        // Expired in the past -> expired
        accountRole.setExpiresAt(LocalDateTime.now().minusDays(1));
        assertThat(accountRole.isExpired()).isTrue();

        // Future expiry -> not expired
        accountRole.setExpiresAt(LocalDateTime.now().plusDays(7));
        assertThat(accountRole.isExpired()).isFalse();
    }
}
