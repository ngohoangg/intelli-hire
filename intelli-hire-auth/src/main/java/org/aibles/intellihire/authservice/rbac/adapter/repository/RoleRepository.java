package org.aibles.intellihire.authservice.rbac.adapter.repository;

import org.aibles.intellihire.authservice.rbac.domain.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(String name);

    boolean existsByName(String name);

    List<RoleEntity> findByIsActiveTrue();

    List<RoleEntity> findByIsSystemTrue();
}
