package org.aibles.intellihire.authservice.admin.usecase;

import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountStatus;
import org.aibles.intellihire.authservice.admin.usecase.command.ListAccountsCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminListAccountsUseCaseImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AdminListAccountsUseCaseImpl adminListAccountsUseCase;

    @Test
    @DisplayName("Should return paginated accounts")
    void execute_success() {
        AccountEntity account = new AccountEntity();
        account.setId(1L);
        account.setEmail("user@example.com");

        Page<AccountEntity> page = new PageImpl<>(List.of(account));
        when(accountRepository.findAll(any(Pageable.class))).thenReturn(page);

        ListAccountsCommand command = new ListAccountsCommand(0, 10, AccountStatus.ACTIVE, "user");
        Page<AccountEntity> result = adminListAccountsUseCase.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("user@example.com");
        verify(accountRepository).findAll(any(Pageable.class));
    }
}
