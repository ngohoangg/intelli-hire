package org.aibles.intellihire.authservice.auth.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.auth.domain.InvalidTokenException;
import org.aibles.intellihire.authservice.shared.config.properties.JwtProperties;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_PERMISSIONS = "permissions";
    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String TOKEN_TYPE_ACCESS = "ACCESS";
    private static final String TOKEN_TYPE_REFRESH = "REFRESH";

    private final JwtProperties jwtProperties;
    private SecretKey signingKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtTokenBundle issueTokenPair(
            Long accountId,
            String email,
            String username,
            Collection<String> roles,
            Collection<String> permissions
    ) {
        String refreshTokenId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        String accessToken = buildAccessToken(accountId, email, username, roles, permissions, now);
        String refreshToken = buildRefreshToken(accountId, email, refreshTokenId, now);

        return new JwtTokenBundle(
                accessToken,
                refreshToken,
                refreshTokenId,
                jwtProperties.getAccessTokenExpirationSeconds(),
                jwtProperties.getRefreshTokenExpirationSeconds()
        );
    }

    private String buildAccessToken(
            Long accountId,
            String email,
            String username,
            Collection<String> roles,
            Collection<String> permissions,
            Instant now
    ) {
        Instant expiration = now.plusSeconds(jwtProperties.getAccessTokenExpirationSeconds());

        return Jwts.builder()
                .subject(String.valueOf(accountId))
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getAccessTokenAudience()).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_ROLES, new ArrayList<>(roles))
                .claim(CLAIM_PERMISSIONS, new ArrayList<>(permissions))
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
                .signWith(signingKey)
                .compact();
    }

    private String buildRefreshToken(Long accountId, String email, String tokenId, Instant now) {
        Instant expiration = now.plusSeconds(jwtProperties.getRefreshTokenExpirationSeconds());

        return Jwts.builder()
                .id(tokenId)
                .subject(String.valueOf(accountId))
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getRefreshTokenAudience()).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
                .signWith(signingKey)
                .compact();
    }

    public AccessTokenClaims parseAndValidateAccessToken(String token) {
        try {
            Claims claims = parseToken(token);

            String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
            if (!TOKEN_TYPE_ACCESS.equals(tokenType)) {
                throw new InvalidTokenException("Loại token không hợp lệ");
            }

            validateAudience(claims, jwtProperties.getAccessTokenAudience());

            Long accountId = Long.valueOf(claims.getSubject());
            String email = claims.get(CLAIM_EMAIL, String.class);
            String username = claims.get(CLAIM_USERNAME, String.class);

            @SuppressWarnings("unchecked")
            List<String> roles = claims.get(CLAIM_ROLES, List.class);
            @SuppressWarnings("unchecked")
            List<String> permissions = claims.get(CLAIM_PERMISSIONS, List.class);

            return new AccessTokenClaims(
                    accountId,
                    email,
                    username,
                    roles != null ? roles : Collections.emptyList(),
                    permissions != null ? permissions : Collections.emptyList()
            );
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid access token: {}", e.getMessage());
            throw new InvalidTokenException("Token không hợp lệ hoặc đã hết hạn");
        }
    }

    public RefreshTokenClaims parseAndValidateRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);

            String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
            if (!TOKEN_TYPE_REFRESH.equals(tokenType)) {
                throw new InvalidTokenException("Loại token không hợp lệ");
            }

            validateAudience(claims, jwtProperties.getRefreshTokenAudience());

            return new RefreshTokenClaims(
                    claims.getId(),
                    Long.valueOf(claims.getSubject()),
                    claims.get(CLAIM_EMAIL, String.class)
            );
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid refresh token: {}", e.getMessage());
            throw new InvalidTokenException("Refresh token không hợp lệ hoặc đã hết hạn");
        }
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private void validateAudience(Claims claims, String expectedAudience) {
        Set<String> audience = claims.getAudience();
        if (audience == null || !audience.contains(expectedAudience)) {
            throw new InvalidTokenException("Đối tượng token không hợp lệ");
        }
    }

    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.getAccessTokenExpirationSeconds();
    }

    public long getRefreshTokenExpirationSeconds() {
        return jwtProperties.getRefreshTokenExpirationSeconds();
    }
}
