package org.aibles.intellihire.authservice.admin.usecase;

import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.admin.usecase.command.UpdateAccountStatusCommand;

public interface AdminUpdateAccountStatusUseCase {
    AccountEntity execute(UpdateAccountStatusCommand command);
}
