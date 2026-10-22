package org.aibles.intellihire.authservice.auth.usecase;

import org.aibles.intellihire.authservice.auth.usecase.command.LoginCommand;

import java.util.List;

public interface LoginUseCase {
    LoginResponse execute(LoginCommand command);

    record LoginResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            long refreshExpiresIn,
            Long accountId,
            String email,
            String username,
            List<String> roles
    ) {
        public static final String TOKEN_TYPE_BEARER = "Bearer";
    }
}
