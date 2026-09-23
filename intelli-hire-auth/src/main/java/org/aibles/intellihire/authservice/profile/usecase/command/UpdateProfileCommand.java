package org.aibles.intellihire.authservice.profile.usecase.command;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileCommand(
        Long accountId,

        @Size(max = 50, message = "First name must be at most 50 characters")
        String firstName,

        @Size(max = 50, message = "Middle name must be at most 50 characters")
        String middleName,

        @Size(max = 50, message = "Last name must be at most 50 characters")
        String lastName,

        String avatarUrl,

        @Size(max = 20, message = "Phone must be at most 20 characters")
        String phone,

        @Size(max = 255, message = "Address must be at most 255 characters")
        String address,

        LocalDate birthOfDate,

        @Size(max = 10, message = "Gender must be at most 10 characters")
        String gender,

        String githubUrl,

        String linkedInUrl
) {
}
