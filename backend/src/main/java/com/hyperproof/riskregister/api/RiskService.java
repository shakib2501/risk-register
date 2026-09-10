package com.hyperproof.riskregister.api;

import com.hyperproof.riskregister.risk.Risk;
import com.hyperproof.riskregister.risk.RiskRepository;
import com.hyperproof.riskregister.risk.RiskStatus;
import com.hyperproof.riskregister.scoring.RiskScoring;
import com.hyperproof.riskregister.scoring.SeverityBand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RiskService {

    private final RiskRepository riskRepository;

    public RiskService(RiskRepository riskRepository) {
        this.riskRepository = riskRepository;
    }

    @Transactional
    public RiskResponse create(CreateRiskRequest request) {
        Risk risk = new Risk(
                request.title(),
                request.description(),
                request.category(),
                request.owner(),
                request.likelihood(),
                request.impact()
        );

        if (request.status() != null && request.status() != RiskStatus.OPEN) {
            risk.changeStatus(request.status());
        }

        return toResponse(riskRepository.save(risk));
    }

    private RiskResponse toResponse(Risk risk) {
        int inherentScore = RiskScoring.inherentScore(risk.getLikelihood(), risk.getImpact());
        List<Integer> effectivenessValues = risk.getMitigations().stream()
                .map(mitigation -> mitigation.getEffectiveness())
                .toList();
        int residualScore = RiskScoring.residualScore(inherentScore, effectivenessValues);

        return new RiskResponse(
                risk.getId(),
                risk.getTitle(),
                risk.getDescription(),
                risk.getCategory(),
                risk.getOwner(),
                risk.getLikelihood(),
                risk.getImpact(),
                risk.getStatus(),
                inherentScore,
                residualScore,
                SeverityBand.fromScore(inherentScore),
                SeverityBand.fromScore(residualScore),
                risk.getMitigationCount(),
                risk.getCreatedAt(),
                risk.getUpdatedAt()
        );
    }
}
