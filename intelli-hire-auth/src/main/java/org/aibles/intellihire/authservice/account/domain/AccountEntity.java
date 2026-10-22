package org.aibles.intellihire.authservice.account.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "last_failed_login_at")
    private LocalDateTime lastFailedLoginAt;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "locked_reason")
    private String lockedReason;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by")
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    // Helper methods
    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean isLocked() {
        return status == AccountStatus.LOCKED;
    }

    public boolean isInactive() {
        return status == AccountStatus.INACTIVE;
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null ? 0 : this.failedLoginAttempts) + 1;
        this.lastFailedLoginAt = LocalDateTime.now();
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lastFailedLoginAt = null;
    }

    public void lock(String reason) {
        this.status = AccountStatus.LOCKED;
        this.lockedAt = LocalDateTime.now();
        this.lockedReason = reason;
    }

    public void unlock() {
        this.status = AccountStatus.ACTIVE;
        this.lockedAt = null;
        this.lockedReason = null;
        resetFailedLoginAttempts();
    }

    public void recordSuccessfulLogin() {
        this.lastLoginAt = LocalDateTime.now();
        resetFailedLoginAttempts();
    }
}
