package org.aibles.intellihire.authservice.account.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountNotFoundException;
import org.aibles.intellihire.authservice.account.usecase.command.DeleteAccountCommand;
import org.aibles.intellihire.authservice.auth.adapter.repository.RefreshTokenRepository;
import org.aibles.intellihire.authservice.auth.domain.InvalidCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteAccountUseCaseImpl implements DeleteAccountUseCase {

    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void execute(DeleteAccountCommand command) {
        log.debug("Processing account deletion for account: {}", command.accountId());

        AccountEntity account = accountRepository.findById(command.accountId())
                .orElseThrow(AccountNotFoundException::new);

        // Verify password
        if (!passwordEncoder.matches(command.password(), account.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // Revoke all refresh tokens
        refreshTokenRepository.revokeAllByAccountId(
                account.getId(),
                LocalDateTime.now(),
                "account_deleted"
        );

        // Delete account (cascades handle related tables)
        accountRepository.delete(account);

        log.info("Account deleted: {}", command.accountId());
    }
}
