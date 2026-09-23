package org.aibles.intellihire.authservice.oauth.client;

import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.oauth.domain.OAuthProviderNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OAuthClientFactory {

    private final Map<String, OAuthClient> clients;

    public OAuthClientFactory(List<OAuthClient> clientList) {
        this.clients = clientList.stream()
                .collect(Collectors.toMap(OAuthClient::getProviderCode, Function.identity()));
        log.info("Registered OAuth clients: {}", clients.keySet());
    }

    public OAuthClient getClient(String providerCode) {
        OAuthClient client = clients.get(providerCode.toLowerCase());
        if (client == null) {
            log.error("No OAuth client found for provider: {}", providerCode);
            throw new OAuthProviderNotFoundException();
        }
        return client;
    }

    public boolean hasClient(String providerCode) {
        return clients.containsKey(providerCode.toLowerCase());
    }
}
