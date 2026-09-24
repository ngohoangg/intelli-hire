package org.aibles.intellihire.authservice.profile.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequestDTO {

    @Size(max = 50, message = "Tên không được vượt quá 50 ký tự")
    @JsonProperty("first_name")
    private String firstName;

    @Size(max = 50, message = "Tên đệm không được vượt quá 50 ký tự")
    @JsonProperty("middle_name")
    private String middleName;

    @Size(max = 50, message = "Họ không được vượt quá 50 ký tự")
    @JsonProperty("last_name")
    private String lastName;

    @Pattern(regexp = "^(https?://.*)?$", message = "URL ảnh đại diện phải là URL HTTP/HTTPS hợp lệ")
    @JsonProperty("avatar_url")
    private String avatarUrl;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    @JsonProperty("birth_of_date")
    private LocalDate birthOfDate;

    @Size(max = 10, message = "Giới tính không được vượt quá 10 ký tự")
    private String gender;

    @Pattern(regexp = "^(https?://github\\.com/.*)?$", message = "Phải là URL GitHub hợp lệ")
    @JsonProperty("github_url")
    private String githubUrl;

    @Pattern(regexp = "^(https?://(www\\.)?linkedin\\.com/.*)?$", message = "Phải là URL LinkedIn hợp lệ")
    @JsonProperty("linked_in_url")
    private String linkedInUrl;
}
