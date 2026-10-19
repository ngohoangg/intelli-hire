package org.aibles.intellihire.authservice.auth.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountStatus;
import org.aibles.intellihire.authservice.account.domain.EmailAlreadyExistsException;
import org.aibles.intellihire.authservice.account.domain.UsernameAlreadyExistsException;
import org.aibles.intellihire.authservice.auth.adapter.repository.EmailVerificationTokenRepository;
import org.aibles.intellihire.authservice.auth.domain.EmailVerificationTokenEntity;
import org.aibles.intellihire.authservice.auth.usecase.command.RegisterCommand;
import org.aibles.intellihire.authservice.profile.adapter.repository.UserProfileRepository;
import org.aibles.intellihire.authservice.profile.domain.UserProfileEntity;
import org.aibles.intellihire.authservice.rbac.adapter.repository.AccountAuthorizationRepository;
import org.aibles.intellihire.authservice.shared.email.EmailService;
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
public class RegisterUseCaseImpl implements RegisterUseCase {

    private static final int VERIFICATION_TOKEN_EXPIRATION_HOURS = 24;

    private final AccountRepository accountRepository;
    private final UserProfileRepository userProfileRepository;
    private final AccountAuthorizationRepository accountAuthorizationRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    @Transactional
    public RegisterResponse execute(RegisterCommand command) {
        log.debug("Processing registration for email: {}", command.email());

        // Validate password match
        if (!command.password().equals(command.confirmPassword())) {
            throw new BaseException(ErrorCode.PASSWORD_MISMATCH) {};
        }

        // Validate email uniqueness
        String normalizedEmail = command.email().toLowerCase().trim();
        if (accountRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        // Validate username uniqueness
        String normalizedUsername = command.username().toLowerCase().trim();
        if (accountRepository.existsByUsername(normalizedUsername)) {
            throw new UsernameAlreadyExistsException(normalizedUsername);
        }

        // Create account
        AccountEntity account = new AccountEntity();
        account.setEmail(normalizedEmail);
        account.setUsername(normalizedUsername);
        account.setPassword(passwordEncoder.encode(command.password()));
        account.setStatus(AccountStatus.ACTIVE);

        AccountEntity savedAccount = accountRepository.save(account);
        log.info("Account created with ID: {}", savedAccount.getId());

        // Create user profile
        UserProfileEntity profile = new UserProfileEntity();
        profile.setAccountId(savedAccount.getId());
        profile.setFirstName(command.firstName());
        profile.setLastName(command.lastName());
        userProfileRepository.save(profile);

        // Assign default role
        accountAuthorizationRepository.assignDefaultRole(savedAccount.getId());
        log.debug("Default role assigned to account: {}", savedAccount.getId());

        // Generate and send email verification token
        String plainToken = SecureTokenGenerator.generateToken();
        String tokenHash = SecureTokenGenerator.hashToken(plainToken);

        EmailVerificationTokenEntity verificationToken = new EmailVerificationTokenEntity();
        verificationToken.setAccountId(savedAccount.getId());
        verificationToken.setTokenHash(tokenHash);
        verificationToken.setEmail(normalizedEmail);
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(VERIFICATION_TOKEN_EXPIRATION_HOURS));
        emailVerificationTokenRepository.save(verificationToken);

        emailService.sendEmailVerificationEmail(normalizedEmail, plainToken);
        log.info("Verification email sent to: {}", normalizedEmail);

        return new RegisterResponse(
                savedAccount.getId(),
                savedAccount.getEmail(),
                savedAccount.getUsername(),
                "Registration successful. Please check your email to verify your account."
        );
    }
}
