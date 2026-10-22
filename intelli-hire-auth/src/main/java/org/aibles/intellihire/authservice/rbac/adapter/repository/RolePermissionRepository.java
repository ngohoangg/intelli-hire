package org.aibles.intellihire.authservice.rbac.adapter.repository;

import org.aibles.intellihire.authservice.rbac.domain.RolePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, Long> {

    List<RolePermissionEntity> findByRoleId(Long roleId);

    List<RolePermissionEntity> findByPermissionId(Long permissionId);

    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

    void deleteByRoleIdAndPermissionId(Long roleId, Long permissionId);

    @Query("SELECT rp FROM RolePermissionEntity rp WHERE rp.roleId IN :roleIds")
    List<RolePermissionEntity> findByRoleIdIn(@Param("roleIds") Collection<Long> roleIds);
}
