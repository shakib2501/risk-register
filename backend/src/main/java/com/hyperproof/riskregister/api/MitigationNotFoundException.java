package com.hyperproof.riskregister.api;

public class MitigationNotFoundException extends RuntimeException {

    public MitigationNotFoundException(Long mitigationId) {
        super("Mitigation with id " + mitigationId + " was not found");
    }
}
