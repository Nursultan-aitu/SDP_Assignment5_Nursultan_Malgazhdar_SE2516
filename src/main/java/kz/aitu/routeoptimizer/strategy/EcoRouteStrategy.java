package kz.aitu.routeoptimizer.strategy;

import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.domain.Route;

public final class EcoRouteStrategy extends AbstractScoredStrategy {

    private static final double EMISSION_WEIGHT = 0.75;
    private static final double DISTANCE_WEIGHT = 0.25;

    @Override
    public String name() {
        return "Eco";
    }

    @Override
    protected double score(Route route, DeliveryRequest request) {
        return route.carbonEmissionKg() * EMISSION_WEIGHT
                + route.distanceKm() * DISTANCE_WEIGHT;
    }

    @Override
    protected String explanation(Route route, DeliveryRequest request, double score) {
        return "Selected %s: %.2f kg emissions x %.2f + %.1f km x %.2f = %.3f."
                .formatted(
                        route.id(),
                        route.carbonEmissionKg(),
                        EMISSION_WEIGHT,
                        route.distanceKm(),
                        DISTANCE_WEIGHT,
                        score
                );
    }
}
