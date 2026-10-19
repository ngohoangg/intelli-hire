package org.aibles.intellihire.authservice.auth.adapter.repository;

import jakarta.persistence.LockModeType;
import org.aibles.intellihire.authservice.auth.domain.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByTokenId(String tokenId);

    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.accountId = :accountId " +
            "AND r.revokedAt IS NULL AND r.expiresAt > :now ORDER BY r.createdAt DESC")
    List<RefreshTokenEntity> findActiveByAccountId(
            @Param("accountId") Long accountId,
            @Param("now") LocalDateTime now
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.tokenId = :tokenId")
    Optional<RefreshTokenEntity> findByTokenIdWithLock(@Param("tokenId") String tokenId);

    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.revokedAt = :revokedAt, r.revokedReason = :reason " +
            "WHERE r.accountId = :accountId AND r.revokedAt IS NULL")
    int revokeAllByAccountId(
            @Param("accountId") Long accountId,
            @Param("revokedAt") LocalDateTime revokedAt,
            @Param("reason") String reason
    );

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.expiresAt < :before")
    int deleteExpiredTokens(@Param("before") LocalDateTime before);
}
