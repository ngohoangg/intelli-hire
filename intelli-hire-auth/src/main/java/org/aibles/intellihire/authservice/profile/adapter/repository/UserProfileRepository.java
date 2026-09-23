package org.aibles.intellihire.authservice.profile.adapter.repository;

import org.aibles.intellihire.authservice.profile.domain.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {

    Optional<UserProfileEntity> findByAccountId(Long accountId);

    boolean existsByAccountId(Long accountId);

    boolean existsByPhone(String phone);

    void deleteByAccountId(Long accountId);
}
