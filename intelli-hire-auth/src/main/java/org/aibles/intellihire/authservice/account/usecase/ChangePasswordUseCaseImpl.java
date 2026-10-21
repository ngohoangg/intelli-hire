package org.aibles.intellihire.authservice.account.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountNotFoundException;
import org.aibles.intellihire.authservice.account.usecase.command.ChangePasswordCommand;
import org.aibles.intellihire.authservice.auth.domain.InvalidCredentialsException;
import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangePasswordUseCaseImpl implements ChangePasswordUseCase {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void execute(ChangePasswordCommand command) {
        log.debug("Processing password change for account: {}", command.accountId());

        if (!command.newPassword().equals(command.confirmPassword())) {
            throw new BaseException(ErrorCode.PASSWORD_MISMATCH) {};
        }

        AccountEntity account = accountRepository.findById(command.accountId())
                .orElseThrow(AccountNotFoundException::new);

        if (!passwordEncoder.matches(command.currentPassword(), account.getPassword())) {
            throw new InvalidCredentialsException();
        }

        if (passwordEncoder.matches(command.newPassword(), account.getPassword())) {
            throw new BaseException(ErrorCode.SAME_PASSWORD) {};
        }

        account.setPassword(passwordEncoder.encode(command.newPassword()));
        accountRepository.save(account);

        log.info("Password changed successfully for account: {}", command.accountId());
    }
}
