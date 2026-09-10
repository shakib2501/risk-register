package com.hyperproof.riskregister.scoring;

public final class RiskScoring {

    private RiskScoring() {
    }

    public static int inherentScore(int likelihood, int impact) {
        return likelihood * impact;
    }
}
