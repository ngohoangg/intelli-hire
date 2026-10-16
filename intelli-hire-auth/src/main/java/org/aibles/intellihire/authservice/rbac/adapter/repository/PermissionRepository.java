package org.aibles.intellihire.authservice.rbac.adapter.repository;

import org.aibles.intellihire.authservice.rbac.domain.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {

    Optional<PermissionEntity> findByCode(String code);

    boolean existsByCode(String code);

    List<PermissionEntity> findByCategory(String category);

    List<PermissionEntity> findByCodeIn(Collection<String> codes);
}
