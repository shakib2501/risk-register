package com.hyperproof.riskregister.risk;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskTest {

    @Test
    void cannotCloseRiskWithoutAMitigation() {
        Risk risk = new Risk();

        assertThatThrownBy(() -> risk.changeStatus(RiskStatus.CLOSED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A risk cannot be closed without at least one mitigation");
    }
}
