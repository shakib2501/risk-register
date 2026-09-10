package com.hyperproof.riskregister.risk;

public class Risk {

    private RiskStatus status = RiskStatus.OPEN;
    private int mitigationCount;

    public void addMitigation() {
        mitigationCount++;
    }

    public void changeStatus(RiskStatus newStatus) {
        if (newStatus == RiskStatus.CLOSED && mitigationCount == 0) {
            throw new IllegalStateException("A risk cannot be closed without at least one mitigation");
        }

        status = newStatus;
    }

    public RiskStatus status() {
        return status;
    }
}
