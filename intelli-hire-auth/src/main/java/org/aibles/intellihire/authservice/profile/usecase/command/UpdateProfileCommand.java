package org.aibles.intellihire.authservice.profile.usecase.command;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileCommand(
        Long accountId,

        @Size(max = 50, message = "Tên không được vượt quá 50 ký tự")
        String firstName,

        @Size(max = 50, message = "Tên đệm không được vượt quá 50 ký tự")
        String middleName,

        @Size(max = 50, message = "Họ không được vượt quá 50 ký tự")
        String lastName,

        String avatarUrl,

        @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
        String phone,

        @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
        String address,

        LocalDate birthOfDate,

        @Size(max = 10, message = "Giới tính không được vượt quá 10 ký tự")
        String gender,

        String githubUrl,

        String linkedInUrl
) {
}
