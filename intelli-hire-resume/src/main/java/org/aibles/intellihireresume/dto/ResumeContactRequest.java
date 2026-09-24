package org.aibles.intellihireresume.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResumeContactRequest {

    @Size(max = 255, message = "Họ tên không được vượt quá 255 ký tự")
    private String fullName;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Pattern(regexp = "^[+]?[\\d\\s\\-]{7,15}$", message = "Số điện thoại không hợp lệ")
    @Size(max = 50, message = "Số điện thoại không được vượt quá 50 ký tự")
    private String phone;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String location;

    @Pattern(
        regexp = "^(https?://)?(www\\.)?linkedin\\.com/.*$",
        message = "URL LinkedIn không hợp lệ"
    )
    @Size(max = 255, message = "URL LinkedIn không được vượt quá 255 ký tự")
    private String linkedinUrl;

    private String otherInfo;
}
