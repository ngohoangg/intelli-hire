package org.aibles.intellihire.authservice.account.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteAccountRequestDTO {
    @NotBlank(message = "Mật khẩu là bắt buộc để xác nhận xóa tài khoản")
    private String password;
}
