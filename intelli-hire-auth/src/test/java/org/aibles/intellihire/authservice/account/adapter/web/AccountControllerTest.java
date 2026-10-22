package org.aibles.intellihire.authservice.account.adapter.web;

import org.aibles.intellihire.authservice.account.adapter.web.dto.ChangePasswordRequestDTO;
import org.aibles.intellihire.authservice.account.adapter.web.dto.DeleteAccountRequestDTO;
import org.aibles.intellihire.authservice.account.usecase.ChangePasswordUseCase;
import org.aibles.intellihire.authservice.account.usecase.DeleteAccountUseCase;
import org.aibles.intellihire.authservice.account.usecase.command.ChangePasswordCommand;
import org.aibles.intellihire.authservice.account.usecase.command.DeleteAccountCommand;
import org.aibles.intellihire.authservice.shared.config.JwtAuthenticationToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private ChangePasswordUseCase changePasswordUseCase;

    @Mock
    private DeleteAccountUseCase deleteAccountUseCase;

    @InjectMocks
    private AccountController accountController;

    @Test
    @DisplayName("Should invoke ChangePasswordUseCase and return 204 No Content")
    void changePassword_success() {
        JwtAuthenticationToken auth = mock(JwtAuthenticationToken.class);
        when(auth.getAccountId()).thenReturn(1L);

        ChangePasswordRequestDTO requestDTO = new ChangePasswordRequestDTO();
        requestDTO.setCurrentPassword("OldPass123!");
        requestDTO.setNewPassword("NewPass123!");
        requestDTO.setConfirmPassword("NewPass123!");

        ResponseEntity<Void> response = accountController.changePassword(auth, requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ArgumentCaptor<ChangePasswordCommand> captor = ArgumentCaptor.forClass(ChangePasswordCommand.class);
        verify(changePasswordUseCase).execute(captor.capture());

        ChangePasswordCommand captured = captor.getValue();
        assertThat(captured.accountId()).isEqualTo(1L);
        assertThat(captured.currentPassword()).isEqualTo("OldPass123!");
        assertThat(captured.newPassword()).isEqualTo("NewPass123!");
        assertThat(captured.confirmPassword()).isEqualTo("NewPass123!");
    }

    @Test
    @DisplayName("Should invoke DeleteAccountUseCase and return 204 No Content")
    void deleteMyAccount_success() {
        JwtAuthenticationToken auth = mock(JwtAuthenticationToken.class);
        when(auth.getAccountId()).thenReturn(2L);

        DeleteAccountRequestDTO requestDTO = new DeleteAccountRequestDTO();
        requestDTO.setPassword("MySecretPassword123!");

        ResponseEntity<Void> response = accountController.deleteMyAccount(auth, requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ArgumentCaptor<DeleteAccountCommand> captor = ArgumentCaptor.forClass(DeleteAccountCommand.class);
        verify(deleteAccountUseCase).execute(captor.capture());

        DeleteAccountCommand captured = captor.getValue();
        assertThat(captured.accountId()).isEqualTo(2L);
        assertThat(captured.password()).isEqualTo("MySecretPassword123!");
    }
}
