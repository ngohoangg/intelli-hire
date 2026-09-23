package org.aibles.intellihire.authservice.oauth.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.auth.adapter.web.dto.LoginResponseDTO;
import org.aibles.intellihire.authservice.auth.usecase.LoginUseCase;
import org.aibles.intellihire.authservice.oauth.adapter.web.dto.*;
import org.aibles.intellihire.authservice.oauth.domain.AccountOAuthLinkEntity;
import org.aibles.intellihire.authservice.oauth.domain.OAuthProviderEntity;
import org.aibles.intellihire.authservice.oauth.usecase.*;
import org.aibles.intellihire.authservice.oauth.usecase.command.LinkOAuthProviderCommand;
import org.aibles.intellihire.authservice.oauth.usecase.command.OAuthCallbackCommand;
import org.aibles.intellihire.authservice.shared.config.JwtAuthenticationToken;
import org.aibles.intellihire.authservice.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/oauth")
@RequiredArgsConstructor
@Tag(name = "OAuth", description = "OAuth2 authentication endpoints")
public class OAuthController {

    private final GetOAuthProvidersUseCase getOAuthProvidersUseCase;
    private final GetOAuthAuthorizationUrlUseCase getOAuthAuthorizationUrlUseCase;
    private final OAuthCallbackUseCase oAuthCallbackUseCase;
    private final LinkOAuthProviderUseCase linkOAuthProviderUseCase;
    private final UnlinkOAuthProviderUseCase unlinkOAuthProviderUseCase;
    private final GetLinkedOAuthProvidersUseCase getLinkedOAuthProvidersUseCase;

    @GetMapping("/providers")
    @Operation(summary = "List OAuth providers", description = "Returns a list of available OAuth providers")
    public ResponseEntity<ApiResponse<List<OAuthProviderResponseDTO>>> getProviders() {
        log.debug("Getting available OAuth providers");

        List<OAuthProviderEntity> providers = getOAuthProvidersUseCase.execute();

        List<OAuthProviderResponseDTO> response = providers.stream()
                .map(this::toProviderResponseDTO)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{provider}/authorize")
    @Operation(summary = "Get OAuth authorization URL", description = "Returns the authorization URL for the specified provider")
    public ResponseEntity<ApiResponse<OAuthAuthorizationUrlResponseDTO>> getAuthorizationUrl(
            @PathVariable String provider
    ) {
        log.debug("Getting authorization URL for provider: {}", provider);

        String state = UUID.randomUUID().toString();
        String authorizationUrl = getOAuthAuthorizationUrlUseCase.execute(provider, state);

        OAuthAuthorizationUrlResponseDTO response = OAuthAuthorizationUrlResponseDTO.builder()
                .authorizationUrl(authorizationUrl)
                .state(state)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{provider}/callback")
    @Operation(summary = "OAuth callback", description = "Handles the OAuth callback and returns JWT tokens")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> callback(
            @PathVariable String provider,
            @Valid @RequestBody OAuthCallbackRequestDTO requestDTO,
            HttpServletRequest request
    ) {
        log.info("OAuth callback for provider: {}", provider);

        OAuthCallbackCommand command = new OAuthCallbackCommand(
                provider,
                requestDTO.getCode(),
                requestDTO.getState(),
                request.getHeader("User-Agent"),
                getClientIp(request)
        );

        LoginUseCase.LoginResponse response = oAuthCallbackUseCase.execute(command);

        return ResponseEntity.ok(ApiResponse.success(toLoginResponseDTO(response)));
    }

    @PostMapping("/{provider}/link")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Link OAuth provider", description = "Links an OAuth provider to the authenticated user's account")
    public ResponseEntity<ApiResponse<Void>> linkProvider(
            @PathVariable String provider,
            @Valid @RequestBody OAuthCallbackRequestDTO requestDTO,
            @AuthenticationPrincipal JwtAuthenticationToken authentication
    ) {
        log.info("Linking OAuth provider {} to account {}", provider, authentication.getAccountId());

        LinkOAuthProviderCommand command = new LinkOAuthProviderCommand(
                authentication.getAccountId(),
                provider,
                requestDTO.getCode()
        );

        linkOAuthProviderUseCase.execute(command);

        return ResponseEntity.ok(ApiResponse.success("OAuth provider linked successfully."));
    }

    @DeleteMapping("/{provider}/unlink")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Unlink OAuth provider", description = "Unlinks an OAuth provider from the authenticated user's account")
    public ResponseEntity<Void> unlinkProvider(
            @PathVariable String provider,
            @AuthenticationPrincipal JwtAuthenticationToken authentication
    ) {
        log.info("Unlinking OAuth provider {} from account {}", provider, authentication.getAccountId());

        unlinkOAuthProviderUseCase.execute(authentication.getAccountId(), provider);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/providers")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get linked OAuth providers", description = "Returns OAuth providers linked to the authenticated user's account")
    public ResponseEntity<ApiResponse<List<LinkedOAuthProviderResponseDTO>>> getLinkedProviders(
            @AuthenticationPrincipal JwtAuthenticationToken authentication
    ) {
        log.debug("Getting linked OAuth providers for account {}", authentication.getAccountId());

        List<AccountOAuthLinkEntity> links = getLinkedOAuthProvidersUseCase.execute(authentication.getAccountId());

        List<LinkedOAuthProviderResponseDTO> response = links.stream()
                .map(this::toLinkedProviderResponseDTO)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private OAuthProviderResponseDTO toProviderResponseDTO(OAuthProviderEntity provider) {
        return OAuthProviderResponseDTO.builder()
                .code(provider.getCode())
                .name(provider.getName())
                .iconUrl(provider.getIconUrl())
                .build();
    }

    private LoginResponseDTO toLoginResponseDTO(LoginUseCase.LoginResponse response) {
        return LoginResponseDTO.builder()
                .accessToken(response.accessToken())
                .refreshToken(response.refreshToken())
                .tokenType(response.tokenType())
                .expiresIn(response.expiresIn())
                .refreshExpiresIn(response.refreshExpiresIn())
                .accountId(response.accountId())
                .email(response.email())
                .username(response.username())
                .roles(response.roles())
                .build();
    }

    private LinkedOAuthProviderResponseDTO toLinkedProviderResponseDTO(AccountOAuthLinkEntity link) {
        return LinkedOAuthProviderResponseDTO.builder()
                .providerCode(link.getProvider().getCode())
                .providerName(link.getProvider().getName())
                .providerEmail(link.getProviderEmail())
                .linkedAt(link.getLinkedAt())
                .lastUsedAt(link.getLastUsedAt())
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
