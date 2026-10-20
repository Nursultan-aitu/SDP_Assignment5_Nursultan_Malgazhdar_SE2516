package kz.aitu.routeoptimizer.domain;

import java.util.Objects;

public record DeliveryRequest(
        String requestId,
        DeliveryPriority priority,
        double maxBudget,
        int maxDeliveryMinutes,
        boolean ecoPreferred,
        double maxEmissionKg
) {

    public DeliveryRequest {
        Objects.requireNonNull(requestId, "requestId must not be null");
        Objects.requireNonNull(priority, "priority must not be null");
        if (requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        if (maxBudget < 0.0) {
            throw new IllegalArgumentException("maxBudget must not be negative");
        }
        if (maxDeliveryMinutes <= 0) {
            throw new IllegalArgumentException("maxDeliveryMinutes must be positive");
        }
        if (maxEmissionKg < 0.0) {
            throw new IllegalArgumentException("maxEmissionKg must not be negative");
        }
    }
}

