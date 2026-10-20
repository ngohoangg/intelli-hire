package org.aibles.intellihire.authservice.profile.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aibles.intellihire.authservice.profile.usecase.command.UpdateProfileCommand;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequestDTO {

    @Size(max = 50, message = "First name must not exceed 50 characters")
    @JsonProperty("first_name")
    private String firstName;

    @Size(max = 50, message = "Middle name must not exceed 50 characters")
    @JsonProperty("middle_name")
    private String middleName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @JsonProperty("birth_of_date")
    private LocalDate birthOfDate;

    @Size(max = 10, message = "Gender must not exceed 10 characters")
    private String gender;

    @JsonProperty("github_url")
    private String githubUrl;

    @JsonProperty("linked_in_url")
    private String linkedInUrl;

    public UpdateProfileCommand toCommand(Long accountId) {
        return new UpdateProfileCommand(
                accountId,
                firstName,
                middleName,
                lastName,
                avatarUrl,
                phone,
                address,
                birthOfDate,
                gender,
                githubUrl,
                linkedInUrl
        );
    }
}
