package com.hyperproof.riskregister.scoring;

import java.util.List;

public final class RiskScoring {

    private RiskScoring() {
    }

    public static int inherentScore(int likelihood, int impact) {
        validateRating(likelihood, "Likelihood");
        validateRating(impact, "Impact");
        return likelihood * impact;
    }

    public static int residualScore(int inherentScore, List<Integer> mitigationEffectiveness) {
        double remainingFraction = 1.0;

        for (Integer effectiveness : mitigationEffectiveness) {
            validateRating(effectiveness, "Mitigation effectiveness");
            remainingFraction *= (6.0 - effectiveness) / 6.0;
        }

        return Math.max(1, (int) Math.ceil(inherentScore * remainingFraction));
    }

    private static void validateRating(int rating, String fieldName) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException(fieldName + " must be between 1 and 5");
        }
    }
}
