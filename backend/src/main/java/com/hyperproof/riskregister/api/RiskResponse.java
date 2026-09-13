package com.hyperproof.riskregister.api;

import com.hyperproof.riskregister.risk.RiskCategory;
import com.hyperproof.riskregister.risk.RiskStatus;
import com.hyperproof.riskregister.scoring.SeverityBand;

import java.time.Instant;
import java.time.LocalDate;

public record RiskResponse(
        Long id,
        String title,
        String description,
        RiskCategory category,
        String owner,
        int likelihood,
        int impact,
        LocalDate nextReviewDate,
        RiskStatus status,
        boolean overdue,
        int inherentScore,
        int residualScore,
        SeverityBand inherentSeverity,
        SeverityBand residualSeverity,
        int mitigationCount,
        Instant createdAt,
        Instant updatedAt
) {
}
