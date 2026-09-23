package org.aibles.intellihire.authservice.auth.adapter.repository;

import org.aibles.intellihire.authservice.auth.domain.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);

    @Query("SELECT t FROM PasswordResetTokenEntity t " +
            "WHERE t.tokenHash = :tokenHash " +
            "AND t.usedAt IS NULL " +
            "AND t.expiresAt > :now")
    Optional<PasswordResetTokenEntity> findValidToken(
            @Param("tokenHash") String tokenHash,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("UPDATE PasswordResetTokenEntity t SET t.usedAt = :usedAt " +
            "WHERE t.accountId = :accountId AND t.usedAt IS NULL")
    int invalidateAllByAccountId(
            @Param("accountId") Long accountId,
            @Param("usedAt") LocalDateTime usedAt
    );

    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity t WHERE t.expiresAt < :before")
    int deleteExpiredTokens(@Param("before") LocalDateTime before);
}
