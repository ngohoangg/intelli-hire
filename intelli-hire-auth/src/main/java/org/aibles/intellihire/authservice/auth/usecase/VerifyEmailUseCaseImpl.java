package org.aibles.intellihire.authservice.auth.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountNotFoundException;
import org.aibles.intellihire.authservice.auth.adapter.repository.EmailVerificationTokenRepository;
import org.aibles.intellihire.authservice.auth.domain.EmailVerificationTokenEntity;
import org.aibles.intellihire.authservice.auth.usecase.command.VerifyEmailCommand;
import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;
import org.aibles.intellihire.authservice.shared.util.SecureTokenGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyEmailUseCaseImpl implements VerifyEmailUseCase {

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public VerifyEmailResponse execute(VerifyEmailCommand command) {
        log.debug("Processing email verification");

        String tokenHash = SecureTokenGenerator.hashToken(command.token());

        // Find valid token
        EmailVerificationTokenEntity tokenEntity = emailVerificationTokenRepository
                .findValidToken(tokenHash, LocalDateTime.now())
                .orElseThrow(() -> new BaseException(ErrorCode.EMAIL_VERIFICATION_TOKEN_INVALID) {});

        // Get account
        AccountEntity account = accountRepository.findById(tokenEntity.getAccountId())
                .orElseThrow(AccountNotFoundException::new);

        // Mark token as verified
        tokenEntity.markAsVerified();
        emailVerificationTokenRepository.save(tokenEntity);

        // Note: Account doesn't have emailVerified field in current schema
        // If needed, add a field to track email verification status
        log.info("Email verified for account: {}", account.getId());

        return new VerifyEmailResponse("Email verified successfully.");
    }
}
