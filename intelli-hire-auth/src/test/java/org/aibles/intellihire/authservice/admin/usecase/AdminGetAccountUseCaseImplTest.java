package org.aibles.intellihire.authservice.admin.usecase;

import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminGetAccountUseCaseImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AdminGetAccountUseCaseImpl adminGetAccountUseCase;

    @Test
    @DisplayName("Should return account when exists")
    void execute_success() {
        AccountEntity account = new AccountEntity();
        account.setId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        AccountEntity result = adminGetAccountUseCase.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(accountRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException when account does not exist")
    void execute_notFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminGetAccountUseCase.execute(999L))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
