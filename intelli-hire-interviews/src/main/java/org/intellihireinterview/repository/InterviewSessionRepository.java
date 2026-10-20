package org.aibles.intellihireinterview.repository;

import org.aibles.intellihireinterview.entity.InterviewSession;
import org.aibles.intellihireinterview.entity.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    List<InterviewSession> findByUserIdOrderByCreatedAtDesc(String userId);
    List<InterviewSession> findByUserIdAndStatus(String userId, SessionStatus status);
    Optional<InterviewSession> findByIdAndUserId(Long id, String userId);
    List<InterviewSession> findByStatusAndStartedAtBefore(SessionStatus status, LocalDateTime threshold);
    Page<InterviewSession> findByUserId(String userId, Pageable pageable);
}
