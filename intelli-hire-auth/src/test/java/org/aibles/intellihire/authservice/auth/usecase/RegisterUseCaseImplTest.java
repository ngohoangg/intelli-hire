package org.aibles.intellihire.authservice.auth.usecase;

import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.EmailAlreadyExistsException;
import org.aibles.intellihire.authservice.account.domain.UsernameAlreadyExistsException;
import org.aibles.intellihire.authservice.auth.adapter.repository.EmailVerificationTokenRepository;
import org.aibles.intellihire.authservice.auth.usecase.command.RegisterCommand;
import org.aibles.intellihire.authservice.profile.adapter.repository.UserProfileRepository;
import org.aibles.intellihire.authservice.rbac.adapter.repository.AccountAuthorizationRepository;
import org.aibles.intellihire.authservice.shared.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseImplTest {

    @Mock private AccountRepository accountRepository;
    @Mock private UserProfileRepository userProfileRepository;
    @Mock private AccountAuthorizationRepository accountAuthorizationRepository;
    @Mock private EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;

    @InjectMocks
    private RegisterUseCaseImpl registerUseCase;

    private RegisterCommand command;

    @BeforeEach
    void setUp() {
        command = new RegisterCommand(
                "test@example.com", "testuser", "Password1",
                "John", "Doe", "127.0.0.1", "Test-Agent"
        );
    }

    @Test
    void execute_success() {
        when(accountRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        AccountEntity saved = new AccountEntity();
        saved.setId(1L);
        saved.setEmail("test@example.com");
        saved.setUsername("testuser");
        when(accountRepository.save(any())).thenReturn(saved);

        RegisterUseCase.RegisterResponse response = registerUseCase.execute(command);

        assertThat(response.accountId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.username()).isEqualTo("testuser");
        verify(emailService).sendEmailVerificationEmail(anyString(), anyString());
        verify(accountAuthorizationRepository).assignDefaultRole(1L);
    }

    @Test
    void execute_emailAlreadyExists_throwsException() {
        when(accountRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> registerUseCase.execute(command))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void execute_usernameAlreadyExists_throwsException() {
        when(accountRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountRepository.existsByUsername(anyString())).thenReturn(true);

        assertThatThrownBy(() -> registerUseCase.execute(command))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }
}
