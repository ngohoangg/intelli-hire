package org.aibles.intellihire.authservice.oauth.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "account_oauth_links",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"provider_id", "provider_user_id"}),
                @UniqueConstraint(columnNames = {"account_id", "provider_id"})
        })
@Getter
@Setter
@NoArgsConstructor
public class AccountOAuthLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(name = "provider_email", length = 255)
    private String providerEmail;

    @Column(name = "linked_at", nullable = false, updatable = false)
    private Instant linkedAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", insertable = false, updatable = false)
    private OAuthProviderEntity provider;

    @PrePersist
    protected void onCreate() {
        this.linkedAt = Instant.now();
        this.lastUsedAt = Instant.now();
    }

    public void updateLastUsedAt() {
        this.lastUsedAt = Instant.now();
    }
}
