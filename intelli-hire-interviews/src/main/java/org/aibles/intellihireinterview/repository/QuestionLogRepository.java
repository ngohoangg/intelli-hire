package org.aibles.intellihireinterview.repository;

import org.aibles.intellihireinterview.entity.QuestionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionLogRepository extends JpaRepository<QuestionLog, Long> {
    List<QuestionLog> findBySessionIdOrderByQuestionOrder(Long sessionId);
}
