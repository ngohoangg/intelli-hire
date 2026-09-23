package org.aibles.intellihirereport.repository;

import jakarta.persistence.LockModeType;
import org.aibles.intellihirereport.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, String> {

    List<UserProgress> findByUserIdOrderBySkillNameAsc(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT up FROM UserProgress up WHERE up.userId = :userId AND up.skillName = :skillName")
    Optional<UserProgress> findByUserIdAndSkillNameForUpdate(
            @Param("userId") Long userId, @Param("skillName") String skillName);

    Optional<UserProgress> findByUserIdAndSkillName(Long userId, String skillName);
}
