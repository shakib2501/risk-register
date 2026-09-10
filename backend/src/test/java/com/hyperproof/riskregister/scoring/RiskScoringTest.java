package com.hyperproof.riskregister.scoring;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RiskScoringTest {

    @Test
    void calculatesInherentScoreFromLikelihoodAndImpact() {
        assertThat(RiskScoring.inherentScore(3, 4)).isEqualTo(12);
    }
}
