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
public class GitHubOAuthClient implements OAuthClient {

    private static final String PROVIDER_CODE = "github";
    private static final String AUTHORIZATION_URL = "https://github.com/login/oauth/authorize";
    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String USER_INFO_URL = "https://api.github.com/user";
    private static final String USER_EMAILS_URL = "https://api.github.com/user/emails";

    private final OAuth2Properties oAuth2Properties;
    private final RestTemplate restTemplate;

    @Override
    public String getProviderCode() {
        return PROVIDER_CODE;
    }

    @Override
    public String getAuthorizationUrl(String state) {
        OAuth2Properties.ProviderConfig config = oAuth2Properties.getGithub();
        String scopes = config.getScopes() != null && !config.getScopes().isBlank()
                ? config.getScopes()
                : "user:email read:user";

        return AUTHORIZATION_URL + "?" +
                "client_id=" + encode(config.getClientId()) +
                "&redirect_uri=" + encode(config.getRedirectUri()) +
                "&scope=" + encode(scopes) +
                "&state=" + encode(state);
    }

    @Override
    public OAuthUserInfo exchangeCodeForUserInfo(String code) {
        log.debug("Exchanging code for user info with GitHub");

        String accessToken = exchangeCodeForAccessToken(code);
        return fetchUserInfo(accessToken);
    }

    private String exchangeCodeForAccessToken(String code) {
        OAuth2Properties.ProviderConfig config = oAuth2Properties.getGithub();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", config.getClientId());
        params.add("client_secret", config.getClientSecret());
        params.add("code", code);
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

            String error = body != null && body.has("error") ? body.get("error").asText() : "Unknown error";
            log.error("Error from GitHub token endpoint: {}", error);
            throw new OAuthCallbackException("Không thể lấy access token từ GitHub: " + error);
        } catch (OAuthCallbackException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error exchanging code for access token with GitHub", e);
            throw new OAuthCallbackException("Không thể trao đổi mã với GitHub: " + e.getMessage());
        }
    }

    private OAuthUserInfo fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

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
                throw new OAuthCallbackException("Phản hồi trống từ thông tin người dùng GitHub");
            }

            String email = body.has("email") && !body.get("email").isNull()
                    ? body.get("email").asText()
                    : fetchPrimaryEmail(accessToken);

            return new OAuthUserInfo(
                    String.valueOf(Objects.requireNonNull(body.get("id")).asLong()),
                    email,
                    body.has("name") && !body.get("name").isNull() ? body.get("name").asText() : body.get("login").asText(),
                    body.has("avatar_url") ? body.get("avatar_url").asText() : null
            );
        } catch (OAuthCallbackException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching user info from GitHub", e);
            throw new OAuthCallbackException("Không thể lấy thông tin người dùng từ GitHub: " + e.getMessage());
        }
    }

    private String fetchPrimaryEmail(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    USER_EMAILS_URL,
                    HttpMethod.GET,
                    request,
                    JsonNode.class
            );

            JsonNode emails = response.getBody();
            if (emails != null && emails.isArray()) {
                for (JsonNode emailNode : emails) {
                    if (emailNode.has("primary") && emailNode.get("primary").asBoolean() &&
                        emailNode.has("verified") && emailNode.get("verified").asBoolean()) {
                        return emailNode.get("email").asText();
                    }
                }
                for (JsonNode emailNode : emails) {
                    if (emailNode.has("verified") && emailNode.get("verified").asBoolean()) {
                        return emailNode.get("email").asText();
                    }
                }
            }

            log.warn("No verified email found for GitHub user");
            return null;
        } catch (Exception e) {
            log.warn("Failed to fetch email from GitHub: {}", e.getMessage());
            return null;
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
