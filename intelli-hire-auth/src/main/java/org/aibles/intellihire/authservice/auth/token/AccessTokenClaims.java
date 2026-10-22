package org.aibles.intellihire.authservice.auth.token;

import java.util.List;

public record AccessTokenClaims(
        Long accountId,
        String email,
        String username,
        List<String> roles,
        List<String> permissions
) {
}
