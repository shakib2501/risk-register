package com.hyperproof.riskregister.api;

public class RiskNotFoundException extends RuntimeException {

    public RiskNotFoundException(Long riskId) {
        super("Risk with id " + riskId + " was not found");
    }
}
