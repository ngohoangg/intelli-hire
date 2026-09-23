package org.aibles.intellihire.authservice.profile.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequestDTO {

    @Size(max = 50, message = "First name must be at most 50 characters")
    @JsonProperty("first_name")
    private String firstName;

    @Size(max = 50, message = "Middle name must be at most 50 characters")
    @JsonProperty("middle_name")
    private String middleName;

    @Size(max = 50, message = "Last name must be at most 50 characters")
    @JsonProperty("last_name")
    private String lastName;

    @Pattern(regexp = "^(https?://.*)?$", message = "Avatar URL must be a valid HTTP/HTTPS URL")
    @JsonProperty("avatar_url")
    private String avatarUrl;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone;

    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @JsonProperty("birth_of_date")
    private LocalDate birthOfDate;

    @Size(max = 10, message = "Gender must be at most 10 characters")
    private String gender;

    @Pattern(regexp = "^(https?://github\\.com/.*)?$", message = "Must be a valid GitHub URL")
    @JsonProperty("github_url")
    private String githubUrl;

    @Pattern(regexp = "^(https?://(www\\.)?linkedin\\.com/.*)?$", message = "Must be a valid LinkedIn URL")
    @JsonProperty("linked_in_url")
    private String linkedInUrl;
}
