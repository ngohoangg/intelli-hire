package org.aibles.intellihire.authservice.admin.usecase;

import org.aibles.intellihire.authservice.account.domain.AccountEntity;

public interface AdminGetAccountUseCase {
    AccountEntity execute(Long accountId);
}
