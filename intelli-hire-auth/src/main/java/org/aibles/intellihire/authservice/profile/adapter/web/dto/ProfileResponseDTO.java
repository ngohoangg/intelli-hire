package org.aibles.intellihire.authservice.profile.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ProfileResponseDTO {

    private Long id;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("middle_name")
    private String middleName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    private String phone;

    private String address;

    @JsonProperty("birth_of_date")
    private LocalDate birthOfDate;

    private String gender;

    @JsonProperty("github_url")
    private String githubUrl;

    @JsonProperty("linked_in_url")
    private String linkedInUrl;
}
