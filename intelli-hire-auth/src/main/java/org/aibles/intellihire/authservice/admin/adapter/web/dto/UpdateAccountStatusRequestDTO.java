package org.aibles.intellihire.authservice.admin.adapter.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.aibles.intellihire.authservice.account.domain.AccountStatus;

@Data
public class UpdateAccountStatusRequestDTO {

    @NotNull(message = "Trạng thái là bắt buộc")
    private AccountStatus status;

    private String reason;
}
