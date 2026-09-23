package org.aibles.intellihire.authservice.oauth.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OAuthAuthorizationUrlResponseDTO {

    @JsonProperty("authorization_url")
    private String authorizationUrl;

    private String state;
}
