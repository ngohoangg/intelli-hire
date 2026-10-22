package org.aibles.intellihire.authservice.auth.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.auth.adapter.repository.PasswordResetTokenRepository;
import org.aibles.intellihire.authservice.auth.domain.PasswordResetTokenEntity;
import org.aibles.intellihire.authservice.auth.usecase.command.ForgotPasswordCommand;
import org.aibles.intellihire.authservice.shared.email.EmailService;
import org.aibles.intellihire.authservice.shared.util.SecureTokenGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPasswordUseCaseImpl implements ForgotPasswordUseCase {

    private static final int TOKEN_EXPIRATION_HOURS = 1;

    private final AccountRepository accountRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public ForgotPasswordResponse execute(ForgotPasswordCommand command) {
        String normalizedEmail = command.email().toLowerCase().trim();
        log.debug("Processing forgot password for email: {}", normalizedEmail);

        // Always return success message to prevent email enumeration
        String successMessage = "If an account with this email exists, a password reset link has been sent.";

        Optional<AccountEntity> accountOpt = accountRepository.findByEmail(normalizedEmail);
        if (accountOpt.isEmpty()) {
            log.debug("No account found for email: {}", normalizedEmail);
            return new ForgotPasswordResponse(successMessage);
        }

        AccountEntity account = accountOpt.get();

        // Invalidate any existing tokens
        passwordResetTokenRepository.invalidateAllByAccountId(account.getId(), LocalDateTime.now());

        // Generate new token
        String plainToken = SecureTokenGenerator.generateToken();
        String tokenHash = SecureTokenGenerator.hashToken(plainToken);

        // Save token
        PasswordResetTokenEntity tokenEntity = new PasswordResetTokenEntity();
        tokenEntity.setAccountId(account.getId());
        tokenEntity.setTokenHash(tokenHash);
        tokenEntity.setExpiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS));
        tokenEntity.setIpAddress(command.ipAddress());
        passwordResetTokenRepository.save(tokenEntity);

        log.info("Password reset token generated for account: {}", account.getId());

        // Send email
        emailService.sendPasswordResetEmail(account.getEmail(), plainToken);

        return new ForgotPasswordResponse(successMessage);
    }
}
