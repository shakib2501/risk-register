package com.hyperproof.riskregister.scoring;

import java.util.List;

public final class RiskScoring {

    private RiskScoring() {
    }

    public static int inherentScore(int likelihood, int impact) {
        return likelihood * impact;
    }

    public static int residualScore(int inherentScore, List<Integer> mitigationEffectiveness) {
        return inherentScore;
    }
}
