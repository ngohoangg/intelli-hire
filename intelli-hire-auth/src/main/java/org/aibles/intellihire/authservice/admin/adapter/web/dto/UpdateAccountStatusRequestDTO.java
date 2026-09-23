package org.aibles.intellihire.authservice.admin.adapter.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.aibles.intellihire.authservice.account.domain.AccountStatus;

@Data
public class UpdateAccountStatusRequestDTO {

    @NotNull(message = "Status is required")
    private AccountStatus status;

    private String reason;
}
