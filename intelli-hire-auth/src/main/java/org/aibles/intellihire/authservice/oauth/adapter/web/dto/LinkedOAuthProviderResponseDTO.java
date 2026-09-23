package org.aibles.intellihire.authservice.oauth.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class LinkedOAuthProviderResponseDTO {

    @JsonProperty("provider_code")
    private String providerCode;

    @JsonProperty("provider_name")
    private String providerName;

    @JsonProperty("provider_email")
    private String providerEmail;

    @JsonProperty("linked_at")
    private Instant linkedAt;

    @JsonProperty("last_used_at")
    private Instant lastUsedAt;
}
