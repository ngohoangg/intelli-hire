package org.aibles.intellihiregrading.repository;

import org.aibles.intellihiregrading.entity.GradingSession;
import org.aibles.intellihiregrading.entity.enums.GradingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GradingSessionRepository extends JpaRepository<GradingSession, String> {

    Optional<GradingSession> findBySessionId(String sessionId);

    boolean existsBySessionIdAndStatusNot(String sessionId, GradingStatus status);

    Optional<GradingSession> findBySessionIdAndStatus(String sessionId, GradingStatus status);

    Page<GradingSession> findByUserId(String userId, Pageable pageable);

    Page<GradingSession> findByUserIdAndStatus(String userId, GradingStatus status, Pageable pageable);
}
