package org.aibles.intellihirereport.repository;

import org.aibles.intellihirereport.entity.QuestionScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionScoreRepository extends JpaRepository<QuestionScore, String> {

    List<QuestionScore> findByReportId(String reportId);
}
