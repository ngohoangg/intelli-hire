package org.aibles.intellihire.authservice.auth.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {

    @NotBlank(message = "Email hoặc tên đăng nhập là bắt buộc")
    private String identifier;

    @NotBlank(message = "Mật khẩu là bắt buộc")
    private String password;
}
