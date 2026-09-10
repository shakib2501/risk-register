package com.hyperproof.riskregister.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMitigationRequest(
        @NotBlank(message = "Mitigation description is required") String description,
        @NotNull(message = "Mitigation effectiveness is required")
        @Min(value = 1, message = "Mitigation effectiveness must be between 1 and 5")
        @Max(value = 5, message = "Mitigation effectiveness must be between 1 and 5")
        Integer effectiveness
) {
}
