package org.aibles.intellihirereport.repository;

import org.aibles.intellihirereport.entity.InterviewReport;
import org.aibles.intellihirereport.entity.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterviewReportRepository extends JpaRepository<InterviewReport, String> {

    Optional<InterviewReport> findBySessionId(String sessionId);

    boolean existsBySessionId(String sessionId);

    Page<InterviewReport> findByUserId(Long userId, Pageable pageable);

    Page<InterviewReport> findByUserIdAndStatus(Long userId, ReportStatus status, Pageable pageable);

    @Query("SELECT COUNT(r) FROM InterviewReport r WHERE r.userId = :userId")
    int countByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(r.totalScore / r.maxScore * 100) FROM InterviewReport r WHERE r.userId = :userId AND r.maxScore > 0 AND r.totalScore IS NOT NULL")
    Double getAverageScorePercentByUserId(@Param("userId") Long userId);

    @Query("SELECT MAX(r.totalScore / r.maxScore * 100) FROM InterviewReport r WHERE r.userId = :userId AND r.maxScore > 0 AND r.totalScore IS NOT NULL")
    Double getMaxScorePercentByUserId(@Param("userId") Long userId);

    @Query("SELECT MIN(r.totalScore / r.maxScore * 100) FROM InterviewReport r WHERE r.userId = :userId AND r.maxScore > 0 AND r.totalScore IS NOT NULL")
    Double getMinScorePercentByUserId(@Param("userId") Long userId);
}
