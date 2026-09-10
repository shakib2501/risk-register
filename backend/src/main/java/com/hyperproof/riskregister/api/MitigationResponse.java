package com.hyperproof.riskregister.api;

import java.time.Instant;

public record MitigationResponse(
        Long id,
        String description,
        int effectiveness,
        Instant createdAt
) {
}
