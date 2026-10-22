package org.aibles.intellihire.authservice.rbac.adapter.repository;

import org.aibles.intellihire.authservice.rbac.domain.AccountRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AccountRoleRepository extends JpaRepository<AccountRoleEntity, Long> {

    List<AccountRoleEntity> findByAccountId(Long accountId);

    List<AccountRoleEntity> findByRoleId(Long roleId);

    Optional<AccountRoleEntity> findByAccountIdAndRoleId(Long accountId, Long roleId);

    boolean existsByAccountIdAndRoleId(Long accountId, Long roleId);

    void deleteByAccountIdAndRoleId(Long accountId, Long roleId);

    @Query("SELECT ar FROM AccountRoleEntity ar WHERE ar.accountId = :accountId AND (ar.expiresAt IS NULL OR ar.expiresAt > CURRENT_TIMESTAMP)")
    List<AccountRoleEntity> findActiveRolesByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT ar FROM AccountRoleEntity ar JOIN FETCH ar.role WHERE ar.accountId = :accountId")
    List<AccountRoleEntity> findByAccountIdWithRole(@Param("accountId") Long accountId);

    void deleteByAccountId(Long accountId);

    @Query("SELECT ar.accountId FROM AccountRoleEntity ar JOIN ar.role r WHERE r.name IN ('SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    Set<Long> findAccountIdsWithSuperAdminRole();
}
