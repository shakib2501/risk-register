package com.hyperproof.riskregister.risk;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskTest {

    @Test
    void cannotCloseRiskWithoutAMitigation() {
        Risk risk = new Risk();

        assertThatThrownBy(() -> risk.changeStatus(RiskStatus.CLOSED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A risk cannot be closed without at least one mitigation");
    }

    @Test
    void canCloseRiskAfterAMitigationIsAdded() {
        Risk risk = new Risk();
        risk.addMitigation(new Mitigation("Documented control", 3));

        risk.changeStatus(RiskStatus.CLOSED);

        assertThat(risk.status()).isEqualTo(RiskStatus.CLOSED);
    }

    @Test
    void associatesAMitigationWithItsRisk() {
        Risk risk = new Risk(
                "Data loss",
                "A service outage may cause data loss.",
                RiskCategory.OPERATIONAL,
                "Platform team",
                3,
                4
        );
        Mitigation mitigation = new Mitigation("Automated backups", 4);

        risk.addMitigation(mitigation);

        assertThat(risk.getMitigationCount()).isEqualTo(1);
        assertThat(mitigation.getRisk()).isSameAs(risk);
    }
}
