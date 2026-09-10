package com.hyperproof.riskregister.scoring;

import java.util.List;

public final class RiskScoring {

    private RiskScoring() {
    }

    public static int inherentScore(int likelihood, int impact) {
        return likelihood * impact;
    }

    public static int residualScore(int inherentScore, List<Integer> mitigationEffectiveness) {
        double remainingFraction = 1.0;

        for (Integer effectiveness : mitigationEffectiveness) {
            remainingFraction *= (6.0 - effectiveness) / 6.0;
        }

        return Math.max(1, (int) Math.ceil(inherentScore * remainingFraction));
    }
}
