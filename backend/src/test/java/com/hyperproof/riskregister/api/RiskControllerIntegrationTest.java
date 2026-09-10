package com.hyperproof.riskregister.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RiskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
}
