package org.aibles.intellihire.authservice.profile.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aibles.intellihire.authservice.profile.domain.UserProfileEntity;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    public static ProfileResponseDTO fromEntity(UserProfileEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProfileResponseDTO.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .firstName(entity.getFirstName())
                .middleName(entity.getMiddleName())
                .lastName(entity.getLastName())
                .fullName(entity.getFullName())
                .avatarUrl(entity.getAvatarUrl())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .birthOfDate(entity.getBirthOfDate())
                .gender(entity.getGender())
                .githubUrl(entity.getGithubUrl())
                .linkedInUrl(entity.getLinkedInUrl())
                .build();
    }
}
