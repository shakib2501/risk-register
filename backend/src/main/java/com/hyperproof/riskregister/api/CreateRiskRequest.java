package com.hyperproof.riskregister.api;

import com.hyperproof.riskregister.risk.RiskCategory;
import com.hyperproof.riskregister.risk.RiskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRiskRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull RiskCategory category,
        @NotBlank String owner,
        @NotNull @Min(1) @Max(5) Integer likelihood,
        @NotNull @Min(1) @Max(5) Integer impact,
        RiskStatus status
) {
}
