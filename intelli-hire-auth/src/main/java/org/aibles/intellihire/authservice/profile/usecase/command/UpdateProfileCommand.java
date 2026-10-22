package org.aibles.intellihire.authservice.profile.usecase.command;

import java.time.LocalDate;

public record UpdateProfileCommand(
        Long accountId,
        String firstName,
        String middleName,
        String lastName,
        String avatarUrl,
        String phone,
        String address,
        LocalDate birthOfDate,
        String gender,
        String githubUrl,
        String linkedInUrl
) {}
