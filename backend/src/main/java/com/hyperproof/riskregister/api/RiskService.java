package com.hyperproof.riskregister.api;

import com.hyperproof.riskregister.risk.Risk;
import com.hyperproof.riskregister.risk.RiskCategory;
import com.hyperproof.riskregister.risk.Mitigation;
import com.hyperproof.riskregister.risk.RiskRepository;
import com.hyperproof.riskregister.risk.RiskStatus;
import com.hyperproof.riskregister.scoring.RiskScoring;
import com.hyperproof.riskregister.scoring.SeverityBand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Comparator;

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

    @Transactional(readOnly = true)
    public List<RiskResponse> list(RiskCategory category, RiskStatus status) {
        return riskRepository.findAll().stream()
                .filter(risk -> category == null || risk.getCategory() == category)
                .filter(risk -> status == null || risk.getStatus() == status)
                .map(this::toResponse)
                .sorted(Comparator.comparingInt(RiskResponse::residualScore).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public RiskResponse get(Long riskId) {
        Risk risk = riskRepository.findById(riskId)
                .orElseThrow(() -> new RiskNotFoundException(riskId));
        return toResponse(risk);
    }

    @Transactional
    public RiskResponse update(Long riskId, CreateRiskRequest request) {
        Risk risk = riskRepository.findById(riskId)
                .orElseThrow(() -> new RiskNotFoundException(riskId));
        risk.update(
                request.title(),
                request.description(),
                request.category(),
                request.owner(),
                request.likelihood(),
                request.impact(),
                request.status()
        );
        return toResponse(risk);
    }

    @Transactional
    public void delete(Long riskId) {
        Risk risk = riskRepository.findById(riskId)
                .orElseThrow(() -> new RiskNotFoundException(riskId));
        riskRepository.delete(risk);
    }

    @Transactional
    public RiskResponse addMitigation(Long riskId, CreateMitigationRequest request) {
        Risk risk = riskRepository.findById(riskId)
                .orElseThrow(() -> new RiskNotFoundException(riskId));
        risk.addMitigation(new Mitigation(request.description(), request.effectiveness()));
        return toResponse(risk);
    }

    @Transactional(readOnly = true)
    public List<MitigationResponse> listMitigations(Long riskId) {
        Risk risk = riskRepository.findById(riskId)
                .orElseThrow(() -> new RiskNotFoundException(riskId));
        return risk.getMitigations().stream()
                .map(mitigation -> new MitigationResponse(
                        mitigation.getId(),
                        mitigation.getDescription(),
                        mitigation.getEffectiveness(),
                        mitigation.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public MitigationResponse updateMitigation(
            Long riskId,
            Long mitigationId,
            CreateMitigationRequest request
    ) {
        Risk risk = riskRepository.findById(riskId)
                .orElseThrow(() -> new RiskNotFoundException(riskId));
        Mitigation mitigation = findMitigation(risk, mitigationId);
        mitigation.update(request.description(), request.effectiveness());
        return toResponse(mitigation);
    }

    @Transactional
    public void deleteMitigation(Long riskId, Long mitigationId) {
        Risk risk = riskRepository.findById(riskId)
                .orElseThrow(() -> new RiskNotFoundException(riskId));
        risk.removeMitigation(findMitigation(risk, mitigationId));
    }

    private Mitigation findMitigation(Risk risk, Long mitigationId) {
        return risk.getMitigations().stream()
                .filter(mitigation -> mitigation.getId().equals(mitigationId))
                .findFirst()
                .orElseThrow(() -> new MitigationNotFoundException(mitigationId));
    }

    private MitigationResponse toResponse(Mitigation mitigation) {
        return new MitigationResponse(
                mitigation.getId(),
                mitigation.getDescription(),
                mitigation.getEffectiveness(),
                mitigation.getCreatedAt()
        );
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
