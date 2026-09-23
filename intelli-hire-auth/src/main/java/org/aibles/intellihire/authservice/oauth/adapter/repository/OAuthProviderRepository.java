package org.aibles.intellihire.authservice.oauth.adapter.repository;

import org.aibles.intellihire.authservice.oauth.domain.OAuthProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OAuthProviderRepository extends JpaRepository<OAuthProviderEntity, Long> {

    Optional<OAuthProviderEntity> findByCode(String code);

    List<OAuthProviderEntity> findByIsActiveTrue();
}
