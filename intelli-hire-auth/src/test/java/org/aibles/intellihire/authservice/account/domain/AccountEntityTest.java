package org.aibles.intellihire.authservice.account.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountEntityTest {

    @Test
    @DisplayName("Should create AccountEntity with default status ACTIVE and 0 failed attempts")
    void shouldCreateAccountEntityWithDefaults() {
        AccountEntity account = new AccountEntity();
        account.setEmail("candidate@intellihire.com");
        account.setUsername("candidate_pro");

        assertThat(account.getEmail()).isEqualTo("candidate@intellihire.com");
        assertThat(account.getUsername()).isEqualTo("candidate_pro");
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.isActive()).isTrue();
        assertThat(account.isLocked()).isFalse();
        assertThat(account.isInactive()).isFalse();
        assertThat(account.getFailedLoginAttempts()).isZero();
    }

    @Test
    @DisplayName("Should increment and reset failed login attempts")
    void shouldHandleFailedLoginAttempts() {
        AccountEntity account = new AccountEntity();
        assertThat(account.getFailedLoginAttempts()).isZero();

        account.incrementFailedLoginAttempts();
        assertThat(account.getFailedLoginAttempts()).isEqualTo(1);
        assertThat(account.getLastFailedLoginAt()).isNotNull();

        account.incrementFailedLoginAttempts();
        assertThat(account.getFailedLoginAttempts()).isEqualTo(2);

        account.resetFailedLoginAttempts();
        assertThat(account.getFailedLoginAttempts()).isZero();
        assertThat(account.getLastFailedLoginAt()).isNull();
    }

    @Test
    @DisplayName("Should lock and unlock account")
    void shouldLockAndUnlockAccount() {
        AccountEntity account = new AccountEntity();
        account.incrementFailedLoginAttempts();

        account.lock("Quá nhiều lần đăng nhập sai");
        assertThat(account.isLocked()).isTrue();
        assertThat(account.getStatus()).isEqualTo(AccountStatus.LOCKED);
        assertThat(account.getLockedAt()).isNotNull();
        assertThat(account.getLockedReason()).isEqualTo("Quá nhiều lần đăng nhập sai");

        account.unlock();
        assertThat(account.isLocked()).isFalse();
        assertThat(account.isActive()).isTrue();
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getLockedAt()).isNull();
        assertThat(account.getLockedReason()).isNull();
        assertThat(account.getFailedLoginAttempts()).isZero();
    }

    @Test
    @DisplayName("Should record successful login and reset attempts")
    void shouldRecordSuccessfulLogin() {
        AccountEntity account = new AccountEntity();
        account.incrementFailedLoginAttempts();

        account.recordSuccessfulLogin();
        assertThat(account.getLastLoginAt()).isNotNull();
        assertThat(account.getFailedLoginAttempts()).isZero();
    }
}
