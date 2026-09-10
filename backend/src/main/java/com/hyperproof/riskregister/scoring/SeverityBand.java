package com.hyperproof.riskregister.scoring;

public enum SeverityBand {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public static SeverityBand fromScore(int score) {
        if (score <= 5) {
            return LOW;
        }
        if (score <= 12) {
            return MEDIUM;
        }
        if (score <= 19) {
            return HIGH;
        }
        return CRITICAL;
    }
}
