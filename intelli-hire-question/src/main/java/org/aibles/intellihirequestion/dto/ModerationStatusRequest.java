package org.aibles.intellihirequestion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModerationStatusRequest {

    @NotBlank
    private String status;

    @JsonProperty("rejection_reason")
    private String rejectionReason;
}
