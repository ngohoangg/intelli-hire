package org.aibles.intellihire.authservice.account.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.account.adapter.web.dto.ChangePasswordRequestDTO;
import org.aibles.intellihire.authservice.account.adapter.web.dto.DeleteAccountRequestDTO;
import org.aibles.intellihire.authservice.account.usecase.ChangePasswordUseCase;
import org.aibles.intellihire.authservice.account.usecase.DeleteAccountUseCase;
import org.aibles.intellihire.authservice.account.usecase.command.ChangePasswordCommand;
import org.aibles.intellihire.authservice.account.usecase.command.DeleteAccountCommand;
import org.aibles.intellihire.authservice.shared.config.JwtAuthenticationToken;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Account", description = "Account management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final DeleteAccountUseCase deleteAccountUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;

    @PostMapping("/me/change-password")
    @Operation(summary = "Change password", description = "Changes the authenticated user's password. Revokes all active sessions after success.")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal JwtAuthenticationToken authentication,
            @Valid @RequestBody ChangePasswordRequestDTO requestDTO
    ) {
        log.info("Password change request for account: {}", authentication.getAccountId());

        ChangePasswordCommand command = new ChangePasswordCommand(
                authentication.getAccountId(),
                requestDTO.getCurrentPassword(),
                requestDTO.getNewPassword(),
                requestDTO.getConfirmPassword()
        );

        changePasswordUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete account", description = "Permanently deletes the authenticated user's account. Requires password confirmation.")
    public ResponseEntity<Void> deleteMyAccount(
            @AuthenticationPrincipal JwtAuthenticationToken authentication,
            @Valid @RequestBody DeleteAccountRequestDTO requestDTO
    ) {
        log.info("Account deletion request for account: {}", authentication.getAccountId());

        DeleteAccountCommand command = new DeleteAccountCommand(
                authentication.getAccountId(),
                requestDTO.getPassword()
        );

        deleteAccountUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }
}
