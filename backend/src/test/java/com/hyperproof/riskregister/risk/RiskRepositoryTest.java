package com.hyperproof.riskregister.risk;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RiskRepositoryTest {

    @Autowired
    private RiskRepository riskRepository;

    @Test
    void persistsANewRiskWithAuditTimestamps() {
        Risk risk = new Risk(
                "Unpatched production systems",
                "Critical systems may miss security patches.",
                RiskCategory.SECURITY,
                "Security team",
                4,
                5
        );

        Risk savedRisk = riskRepository.saveAndFlush(risk);

        assertThat(savedRisk.getId()).isNotNull();
        assertThat(savedRisk.getTitle()).isEqualTo("Unpatched production systems");
        assertThat(savedRisk.getStatus()).isEqualTo(RiskStatus.OPEN);
        assertThat(savedRisk.getCreatedAt()).isNotNull();
        assertThat(savedRisk.getUpdatedAt()).isNotNull();
    }
}
