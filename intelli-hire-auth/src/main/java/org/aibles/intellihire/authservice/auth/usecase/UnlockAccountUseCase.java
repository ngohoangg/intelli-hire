package org.aibles.intellihire.authservice.auth.usecase;

public interface UnlockAccountUseCase {
    UnlockAccountResponse execute(Long accountId);

    record UnlockAccountResponse(
            Long accountId,
            String message
    ) {
    }
}
