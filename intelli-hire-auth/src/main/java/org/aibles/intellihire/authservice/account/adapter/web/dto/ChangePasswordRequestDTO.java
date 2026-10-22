package org.aibles.intellihire.authservice.account.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequestDTO {

    @NotBlank(message = "Mật khẩu hiện tại là bắt buộc")
    private String currentPassword;

    @NotBlank(message = "Mật khẩu mới là bắt buộc")
    @Size(min = 8, message = "Mật khẩu mới phải có ít nhất 8 ký tự")
    private String newPassword;

    @NotBlank(message = "Mật khẩu xác nhận là bắt buộc")
    private String confirmPassword;
}
