package org.aibles.intellihire.authservice.rbac.adapter.repository;

import org.aibles.intellihire.authservice.rbac.domain.PermissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {

    Optional<PermissionEntity> findByCode(String code);

    boolean existsByCode(String code);

    Page<PermissionEntity> findByIsActiveTrue(Pageable pageable);

    List<PermissionEntity> findByCategory(String category);

    @Query("SELECT DISTINCT p.category FROM PermissionEntity p WHERE p.category IS NOT NULL")
    List<String> findAllCategories();

    @Query("SELECT p FROM PermissionEntity p " +
            "JOIN RolePermissionEntity rp ON p.id = rp.permissionId " +
            "WHERE rp.roleId = :roleId AND p.isActive = true")
    List<PermissionEntity> findByRoleId(@Param("roleId") Long roleId);
}
