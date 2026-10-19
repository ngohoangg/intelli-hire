package org.aibles.intellihire.authservice.auth.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountNotFoundException;
import org.aibles.intellihire.authservice.auth.adapter.repository.PasswordResetTokenRepository;
import org.aibles.intellihire.authservice.auth.adapter.repository.RefreshTokenRepository;
import org.aibles.intellihire.authservice.auth.domain.PasswordResetTokenEntity;
import org.aibles.intellihire.authservice.auth.usecase.command.ResetPasswordCommand;
import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;
import org.aibles.intellihire.authservice.shared.util.SecureTokenGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordUseCaseImpl implements ResetPasswordUseCase {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ResetPasswordResponse execute(ResetPasswordCommand command) {
        log.debug("Processing password reset");

        // Hash the provided token to find it in the database
        String tokenHash = SecureTokenGenerator.hashToken(command.token());

        // Find valid token
        PasswordResetTokenEntity tokenEntity = passwordResetTokenRepository
                .findValidToken(tokenHash, LocalDateTime.now())
                .orElseThrow(() -> new BaseException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID) {});

        // Get account
        AccountEntity account = accountRepository.findById(tokenEntity.getAccountId())
                .orElseThrow(AccountNotFoundException::new);

        // Update password
        account.setPassword(passwordEncoder.encode(command.newPassword()));
        accountRepository.save(account);

        // Mark token as used
        tokenEntity.markAsUsed();
        passwordResetTokenRepository.save(tokenEntity);

        // Revoke all refresh tokens for security
        refreshTokenRepository.revokeAllByAccountId(
                account.getId(),
                LocalDateTime.now(),
                "password_reset"
        );

        log.info("Password reset successful for account: {}", account.getId());

        return new ResetPasswordResponse("Password has been reset successfully. Please login with your new password.");
    }
}
