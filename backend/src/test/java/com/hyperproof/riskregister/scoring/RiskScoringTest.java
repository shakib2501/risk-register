package com.hyperproof.riskregister.scoring;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void compoundsMultipleMitigationsAndNeverReturnsLessThanOne() {
        assertThat(RiskScoring.residualScore(25, List.of(4, 3))).isEqualTo(5);
        assertThat(RiskScoring.residualScore(25, List.of(5, 5))).isEqualTo(1);
    }

    @Test
    void rejectsRatingsOutsideTheSupportedRange() {
        assertThatThrownBy(() -> RiskScoring.inherentScore(0, 3))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RiskScoring.inherentScore(3, 6))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RiskScoring.residualScore(20, List.of(0)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RiskScoring.residualScore(20, List.of(6)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInherentScoresOutsideTheSupportedRange() {
        assertThatThrownBy(() -> RiskScoring.residualScore(0, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RiskScoring.residualScore(26, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
