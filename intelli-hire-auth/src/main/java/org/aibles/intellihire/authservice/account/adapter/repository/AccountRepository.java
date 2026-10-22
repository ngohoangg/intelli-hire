package org.aibles.intellihire.authservice.account.adapter.repository;

import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    Optional<AccountEntity> findByEmail(String email);

    Optional<AccountEntity> findByUsername(String username);

    Optional<AccountEntity> findByEmailOrUsername(String email, String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT a FROM AccountEntity a WHERE (a.email = :identifier OR a.username = :identifier)")
    Optional<AccountEntity> findByEmailOrUsername(@Param("identifier") String identifier);

    @Query("SELECT COUNT(a) > 0 FROM AccountEntity a WHERE a.email = :email AND a.status = :status")
    boolean existsByEmailAndStatus(@Param("email") String email, @Param("status") AccountStatus status);
}
