package kz.aitu.routeoptimizer.domain;

import java.util.Objects;
import java.util.Set;

public record Route(
        String id,
        Location origin,
        Location destination,
        double distanceKm,
        int estimatedMinutes,
        double baseCost,
        double distanceCostPerKm,
        double additionalFees,
        double carbonEmissionKg,
        TrafficLevel trafficLevel,
        WeatherCondition weatherCondition,
        double safetyRating,
        boolean available,
        Set<DeliveryPriority> prohibitedPriorities
) {

    public Route {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(origin, "origin must not be null");
        Objects.requireNonNull(destination, "destination must not be null");
        Objects.requireNonNull(trafficLevel, "trafficLevel must not be null");
        Objects.requireNonNull(weatherCondition, "weatherCondition must not be null");
        Objects.requireNonNull(prohibitedPriorities, "prohibitedPriorities must not be null");

        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (distanceKm <= 0.0) {
            throw new IllegalArgumentException("distanceKm must be positive");
        }
        if (estimatedMinutes <= 0) {
            throw new IllegalArgumentException("estimatedMinutes must be positive");
        }
        if (baseCost < 0.0 || distanceCostPerKm < 0.0 || additionalFees < 0.0) {
            throw new IllegalArgumentException("route costs must not be negative");
        }
        if (carbonEmissionKg < 0.0) {
            throw new IllegalArgumentException("carbonEmissionKg must not be negative");
        }
        if (safetyRating < 0.0 || safetyRating > 10.0) {
            throw new IllegalArgumentException("safetyRating must be between 0 and 10");
        }

        prohibitedPriorities = Set.copyOf(prohibitedPriorities);
    }
}

