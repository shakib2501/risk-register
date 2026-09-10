package com.hyperproof.riskregister.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hyperproof.riskregister.risk.RiskCategory;
import com.hyperproof.riskregister.risk.RiskStatus;

import java.util.List;

@RestController
@RequestMapping("/api/risks")
public class RiskController {

    private final RiskService riskService;

    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RiskResponse create(@Valid @RequestBody CreateRiskRequest request) {
        return riskService.create(request);
    }

    @GetMapping
    public List<RiskResponse> list(
            @RequestParam(required = false) RiskCategory category,
            @RequestParam(required = false) RiskStatus status
    ) {
        return riskService.list(category, status);
    }

    @GetMapping("/{id}")
    public RiskResponse get(@PathVariable Long id) {
        return riskService.get(id);
    }

    @PutMapping("/{id}")
    public RiskResponse update(@PathVariable Long id, @Valid @RequestBody CreateRiskRequest request) {
        return riskService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        riskService.delete(id);
    }

    @PostMapping("/{id}/mitigations")
    @ResponseStatus(HttpStatus.CREATED)
    public RiskResponse addMitigation(
            @PathVariable Long id,
            @Valid @RequestBody CreateMitigationRequest request
    ) {
        return riskService.addMitigation(id, request);
    }
}
