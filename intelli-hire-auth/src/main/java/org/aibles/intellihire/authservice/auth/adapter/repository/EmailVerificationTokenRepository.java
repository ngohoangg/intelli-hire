package org.aibles.intellihire.authservice.auth.adapter.repository;

import org.aibles.intellihire.authservice.auth.domain.EmailVerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationTokenEntity, Long> {

    Optional<EmailVerificationTokenEntity> findByTokenHash(String tokenHash);

    @Query("SELECT t FROM EmailVerificationTokenEntity t " +
            "WHERE t.tokenHash = :tokenHash " +
            "AND t.verifiedAt IS NULL " +
            "AND t.expiresAt > :now")
    Optional<EmailVerificationTokenEntity> findValidToken(
            @Param("tokenHash") String tokenHash,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT t FROM EmailVerificationTokenEntity t " +
            "WHERE t.accountId = :accountId " +
            "AND t.verifiedAt IS NULL " +
            "AND t.expiresAt > :now " +
            "ORDER BY t.createdAt DESC")
    Optional<EmailVerificationTokenEntity> findLatestValidTokenByAccountId(
            @Param("accountId") Long accountId,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("DELETE FROM EmailVerificationTokenEntity t WHERE t.expiresAt < :before")
    int deleteExpiredTokens(@Param("before") LocalDateTime before);
}
