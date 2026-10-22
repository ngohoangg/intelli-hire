package org.aibles.intellihire.authservice.shared.config;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Long accountId;
    private final String email;
    private final String username;

    public JwtAuthenticationToken(
            Long accountId,
            String email,
            String username,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.accountId = accountId;
        this.email = email;
        this.username = username;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this;
    }

    @Override
    public String getName() {
        return email;
    }
}
