package com.hyperproof.riskregister.api;

import com.hyperproof.riskregister.risk.RiskCategory;
import com.hyperproof.riskregister.risk.RiskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

public record CreateRiskRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull RiskCategory category,
        @NotBlank String owner,
        @NotNull(message = "Likelihood is required")
        @Min(value = 1, message = "Likelihood must be between 1 and 5")
        @Max(value = 5, message = "Likelihood must be between 1 and 5") Integer likelihood,
        @NotNull(message = "Impact is required")
        @Min(value = 1, message = "Impact must be between 1 and 5")
        @Max(value = 5, message = "Impact must be between 1 and 5") Integer impact,
        RiskStatus status,
        @Valid CreateMitigationRequest initialMitigation
) {
}
