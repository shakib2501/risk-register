package com.hyperproof.riskregister.scoring;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiskScoringTest {

    @Test
    void calculatesInherentScoreFromLikelihoodAndImpact() {
        assertThat(RiskScoring.inherentScore(3, 4)).isEqualTo(12);
    }

    @Test
    void keepsInherentScoreWhenNoMitigationsExist() {
        assertThat(RiskScoring.residualScore(20, List.of())).isEqualTo(20);
    }

    @Test
    void reducesRiskWithAHighlyEffectiveMitigation() {
        assertThat(RiskScoring.residualScore(20, List.of(5))).isEqualTo(4);
    }
}
