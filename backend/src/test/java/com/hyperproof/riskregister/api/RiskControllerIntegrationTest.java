package com.hyperproof.riskregister.api;

import com.hyperproof.riskregister.risk.Risk;
import com.hyperproof.riskregister.risk.RiskCategory;
import com.hyperproof.riskregister.risk.Mitigation;
import com.hyperproof.riskregister.risk.RiskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class RiskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RiskRepository riskRepository;

    @Test
    void createsRiskAndReturnsCalculatedScores() throws Exception {
        mockMvc.perform(post("/api/risks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Unpatched production systems",
                                  "description": "Critical systems may miss security patches.",
                                  "category": "SECURITY",
                                  "owner": "Security team",
                                  "likelihood": 4,
                                  "impact": 5,
                                  "status": "OPEN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Unpatched production systems"))
                .andExpect(jsonPath("$.inherentScore").value(20))
                .andExpect(jsonPath("$.residualScore").value(20))
                .andExpect(jsonPath("$.residualSeverity").value("CRITICAL"));
    }

    @Test
    void listsRisksFilteredByCategoryAndStatus() throws Exception {
        mockMvc.perform(post("/api/risks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Unpatched production systems",
                                  "description": "Critical systems may miss security patches.",
                                  "category": "SECURITY",
                                  "owner": "Security team",
                                  "likelihood": 4,
                                  "impact": 5,
                                  "status": "OPEN"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/risks")
                        .param("category", "SECURITY")
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Unpatched production systems"));
    }

    @Test
    void returnsClearBadRequestMessagesForInvalidRiskInput() throws Exception {
        mockMvc.perform(post("/api/risks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Unpatched production systems",
                                  "description": "Critical systems may miss security patches.",
                                  "category": "SECURITY",
                                  "owner": "Security team",
                                  "likelihood": 0,
                                  "impact": 5,
                                  "status": "OPEN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Likelihood must be between 1 and 5"));

        mockMvc.perform(post("/api/risks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Unpatched production systems",
                                  "description": "Critical systems may miss security patches.",
                                  "category": "SECURITY",
                                  "owner": "Security team",
                                  "likelihood": 4,
                                  "impact": 5,
                                  "status": "CLOSED"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("A risk cannot be closed without at least one mitigation"));
    }

    @Test
    void retrievesRiskById() throws Exception {
        Risk savedRisk = riskRepository.saveAndFlush(new Risk(
                "Vendor outage",
                "A critical vendor may become unavailable.",
                RiskCategory.OPERATIONAL,
                "Operations team",
                3,
                4
        ));

        mockMvc.perform(get("/api/risks/{id}", savedRisk.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedRisk.getId()))
                .andExpect(jsonPath("$.title").value("Vendor outage"))
                .andExpect(jsonPath("$.inherentScore").value(12));
    }

    @Test
    void updatesAnExistingRisk() throws Exception {
        Risk savedRisk = riskRepository.saveAndFlush(new Risk(
                "Vendor outage",
                "A critical vendor may become unavailable.",
                RiskCategory.OPERATIONAL,
                "Operations team",
                3,
                4
        ));

        mockMvc.perform(put("/api/risks/{id}", savedRisk.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Primary vendor outage",
                                  "description": "A critical vendor may become unavailable.",
                                  "category": "OPERATIONAL",
                                  "owner": "Operations team",
                                  "likelihood": 4,
                                  "impact": 4,
                                  "status": "MITIGATING"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Primary vendor outage"))
                .andExpect(jsonPath("$.status").value("MITIGATING"))
                .andExpect(jsonPath("$.inherentScore").value(16));
    }

    @Test
    void deletesAnExistingRisk() throws Exception {
        Risk savedRisk = riskRepository.saveAndFlush(new Risk(
                "Vendor outage",
                "A critical vendor may become unavailable.",
                RiskCategory.OPERATIONAL,
                "Operations team",
                3,
                4
        ));

        mockMvc.perform(delete("/api/risks/{id}", savedRisk.getId()))
                .andExpect(status().isNoContent());

        assertThat(riskRepository.findById(savedRisk.getId())).isEmpty();
    }

    @Test
    void addsAMitigationAndRecalculatesResidualRisk() throws Exception {
        Risk savedRisk = riskRepository.saveAndFlush(new Risk(
                "Unpatched production systems",
                "Critical systems may miss security patches.",
                RiskCategory.SECURITY,
                "Security team",
                4,
                5
        ));

        mockMvc.perform(post("/api/risks/{id}/mitigations", savedRisk.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Deploy weekly patching automation.",
                                  "effectiveness": 5
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mitigationCount").value(1))
                .andExpect(jsonPath("$.residualScore").value(4))
                .andExpect(jsonPath("$.residualSeverity").value("LOW"));
    }

    @Test
    void listsMitigationsForARisk() throws Exception {
        Risk risk = new Risk(
                "Unpatched production systems",
                "Critical systems may miss security patches.",
                RiskCategory.SECURITY,
                "Security team",
                4,
                5
        );
        risk.addMitigation(new Mitigation("Deploy weekly patching automation.", 5));
        Risk savedRisk = riskRepository.saveAndFlush(risk);

        mockMvc.perform(get("/api/risks/{id}/mitigations", savedRisk.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Deploy weekly patching automation."))
                .andExpect(jsonPath("$[0].effectiveness").value(5));
    }

    @Test
    void updatesAMitigationForARisk() throws Exception {
        Risk risk = new Risk(
                "Unpatched production systems",
                "Critical systems may miss security patches.",
                RiskCategory.SECURITY,
                "Security team",
                4,
                5
        );
        Mitigation mitigation = new Mitigation("Deploy patching automation.", 3);
        risk.addMitigation(mitigation);
        riskRepository.saveAndFlush(risk);

        mockMvc.perform(put("/api/risks/{riskId}/mitigations/{mitigationId}", risk.getId(), mitigation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Deploy verified patching automation.",
                                  "effectiveness": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Deploy verified patching automation."))
                .andExpect(jsonPath("$.effectiveness").value(5));
    }
}
