package kz.aitu.routeoptimizer.service;

import kz.aitu.routeoptimizer.domain.Route;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record RouteDecision(
        String strategyName,
        Route selectedRoute,
        double score,
        String explanation,
        Map<String, String> rejectedRoutes
) {

    public RouteDecision {
        Objects.requireNonNull(strategyName, "strategyName must not be null");
        Objects.requireNonNull(selectedRoute, "selectedRoute must not be null");
        Objects.requireNonNull(explanation, "explanation must not be null");
        Objects.requireNonNull(rejectedRoutes, "rejectedRoutes must not be null");
        rejectedRoutes = Map.copyOf(new LinkedHashMap<>(rejectedRoutes));
    }
}
