package com.hyperproof.riskregister.api;

import com.hyperproof.riskregister.risk.Risk;
import com.hyperproof.riskregister.risk.RiskCategory;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}
