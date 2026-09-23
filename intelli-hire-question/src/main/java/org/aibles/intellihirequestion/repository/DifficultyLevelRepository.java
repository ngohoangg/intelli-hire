package org.aibles.intellihirequestion.repository;

import org.aibles.intellihirequestion.entity.DifficultyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DifficultyLevel entity
 */
@Repository
public interface DifficultyLevelRepository extends JpaRepository<DifficultyLevel, String> {

    /**
     * Find difficulty level by name
     * @param name the name of difficulty level
     * @return an optional of difficulty level
     */
    Optional<DifficultyLevel> findByName(String name);

    /**
     * Check whether difficulty level already exists by name (case-insensitive)
     * @param name difficulty level name
     * @return true if name exists, otherwise false
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Check whether another difficulty level already exists by name (case-insensitive).
     * Excludes the current difficulty level by id.
     *
     * @param name difficulty level name
     * @param id difficulty level id to exclude
     * @return true if another difficulty level with the same name exists, otherwise false
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);

    /**
     * Find all difficulty levels ordered by weight ascending
     * @return a list of difficulty levels
     */
    List<DifficultyLevel> findAllByOrderByWeightAsc();
}
