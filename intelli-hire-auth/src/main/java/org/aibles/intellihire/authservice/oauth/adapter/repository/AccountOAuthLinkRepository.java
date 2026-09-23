package org.aibles.intellihire.authservice.oauth.adapter.repository;

import org.aibles.intellihire.authservice.oauth.domain.AccountOAuthLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountOAuthLinkRepository extends JpaRepository<AccountOAuthLinkEntity, Long> {

    Optional<AccountOAuthLinkEntity> findByProviderIdAndProviderUserId(Long providerId, String providerUserId);

    Optional<AccountOAuthLinkEntity> findByAccountIdAndProviderId(Long accountId, Long providerId);

    List<AccountOAuthLinkEntity> findByAccountId(Long accountId);

    boolean existsByAccountIdAndProviderId(Long accountId, Long providerId);

    @Query("SELECT a FROM AccountOAuthLinkEntity a JOIN FETCH a.provider WHERE a.accountId = :accountId")
    List<AccountOAuthLinkEntity> findByAccountIdWithProvider(@Param("accountId") Long accountId);

    void deleteByAccountIdAndProviderId(Long accountId, Long providerId);
}
