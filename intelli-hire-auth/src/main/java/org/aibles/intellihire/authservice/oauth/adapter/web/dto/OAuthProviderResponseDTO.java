package org.aibles.intellihire.authservice.oauth.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OAuthProviderResponseDTO {

    private String code;
    private String name;

    @JsonProperty("icon_url")
    private String iconUrl;
}
