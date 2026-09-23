package org.aibles.intellihire.authservice.admin.usecase;

import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.admin.usecase.command.ListAccountsCommand;
import org.springframework.data.domain.Page;

public interface AdminListAccountsUseCase {
    Page<AccountEntity> execute(ListAccountsCommand command);
}
