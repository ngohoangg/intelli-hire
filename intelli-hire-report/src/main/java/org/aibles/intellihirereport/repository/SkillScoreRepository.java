package org.aibles.intellihirereport.repository;

import org.aibles.intellihirereport.entity.SkillScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillScoreRepository extends JpaRepository<SkillScore, String> {

    List<SkillScore> findByReportId(String reportId);
}
