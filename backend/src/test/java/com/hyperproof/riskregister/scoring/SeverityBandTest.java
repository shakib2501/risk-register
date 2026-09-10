package com.hyperproof.riskregister.scoring;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SeverityBandTest {

    @Test
    void classifiesScoresAtEachSeverityBoundary() {
        assertThat(SeverityBand.fromScore(1)).isEqualTo(SeverityBand.LOW);
        assertThat(SeverityBand.fromScore(5)).isEqualTo(SeverityBand.LOW);
        assertThat(SeverityBand.fromScore(6)).isEqualTo(SeverityBand.MEDIUM);
        assertThat(SeverityBand.fromScore(12)).isEqualTo(SeverityBand.MEDIUM);
        assertThat(SeverityBand.fromScore(13)).isEqualTo(SeverityBand.HIGH);
        assertThat(SeverityBand.fromScore(19)).isEqualTo(SeverityBand.HIGH);
        assertThat(SeverityBand.fromScore(20)).isEqualTo(SeverityBand.CRITICAL);
        assertThat(SeverityBand.fromScore(25)).isEqualTo(SeverityBand.CRITICAL);
    }
}
