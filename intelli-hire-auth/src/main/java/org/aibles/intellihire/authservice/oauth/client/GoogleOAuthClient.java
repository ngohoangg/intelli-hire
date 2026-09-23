package org.aibles.intellihire.authservice.oauth.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.oauth.domain.OAuthCallbackException;
import org.aibles.intellihire.authservice.oauth.domain.OAuthUserInfo;
import org.aibles.intellihire.authservice.shared.config.properties.OAuth2Properties;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOAuthClient implements OAuthClient {

    private static final String PROVIDER_CODE = "google";
    private static final String AUTHORIZATION_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    private final OAuth2Properties oAuth2Properties;
    private final RestTemplate restTemplate;

    @Override
    public String getProviderCode() {
        return PROVIDER_CODE;
    }

    @Override
    public String getAuthorizationUrl(String state) {
        OAuth2Properties.ProviderConfig config = oAuth2Properties.getGoogle();
        String scopes = config.getScopes() != null && !config.getScopes().isBlank()
                ? config.getScopes()
                : "openid email profile";

        return AUTHORIZATION_URL + "?" +
                "client_id=" + encode(config.getClientId()) +
                "&redirect_uri=" + encode(config.getRedirectUri()) +
                "&response_type=code" +
                "&scope=" + encode(scopes) +
                "&state=" + encode(state) +
                "&access_type=offline" +
                "&prompt=consent";
    }

    @Override
    public OAuthUserInfo exchangeCodeForUserInfo(String code) {
        log.debug("Exchanging code for user info with Google");

        String accessToken = exchangeCodeForAccessToken(code);
        return fetchUserInfo(accessToken);
    }

    private String exchangeCodeForAccessToken(String code) {
        OAuth2Properties.ProviderConfig config = oAuth2Properties.getGoogle();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", config.getClientId());
        params.add("client_secret", config.getClientSecret());
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", config.getRedirectUri());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    TOKEN_URL,
                    HttpMethod.POST,
                    request,
                    JsonNode.class
            );

            JsonNode body = response.getBody();
            if (body != null && body.has("access_token")) {
                return body.get("access_token").asText();
            }

            log.error("No access token in Google response");
            throw new OAuthCallbackException("Failed to get access token from Google");
        } catch (Exception e) {
            log.error("Error exchanging code for access token with Google", e);
            throw new OAuthCallbackException("Failed to exchange code with Google: " + e.getMessage());
        }
    }

    private OAuthUserInfo fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    USER_INFO_URL,
                    HttpMethod.GET,
                    request,
                    JsonNode.class
            );

            JsonNode body = response.getBody();
            if (body == null) {
                throw new OAuthCallbackException("Empty response from Google user info");
            }

            return new OAuthUserInfo(
                    Objects.requireNonNull(body.get("id")).asText(),
                    body.has("email") ? body.get("email").asText() : null,
                    body.has("name") ? body.get("name").asText() : null,
                    body.has("picture") ? body.get("picture").asText() : null
            );
        } catch (OAuthCallbackException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching user info from Google", e);
            throw new OAuthCallbackException("Failed to fetch user info from Google: " + e.getMessage());
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
