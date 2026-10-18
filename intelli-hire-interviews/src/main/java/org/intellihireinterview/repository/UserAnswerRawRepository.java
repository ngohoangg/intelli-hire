package org.aibles.intellihireinterview.repository;

import org.aibles.intellihireinterview.entity.UserAnswerRaw;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserAnswerRawRepository extends JpaRepository<UserAnswerRaw, Long> {
    List<UserAnswerRaw> findBySessionIdOrderBySubmittedAt(Long sessionId);
    boolean existsBySessionIdAndQuestionId(Long sessionId, String questionId);
}
