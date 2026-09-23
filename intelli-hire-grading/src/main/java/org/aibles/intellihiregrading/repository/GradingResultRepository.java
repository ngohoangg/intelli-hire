package org.aibles.intellihiregrading.repository;

import org.aibles.intellihiregrading.entity.GradingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface GradingResultRepository extends JpaRepository<GradingResult, String> {

    List<GradingResult> findByGradingSessionId(String gradingSessionId);

    @Modifying
    @Transactional
    void deleteByGradingSessionId(String gradingSessionId);
}
