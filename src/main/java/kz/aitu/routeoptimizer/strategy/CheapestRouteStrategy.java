package kz.aitu.routeoptimizer.strategy;

import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.domain.Route;
import kz.aitu.routeoptimizer.service.RouteMetrics;

public final class CheapestRouteStrategy extends AbstractScoredStrategy {

    @Override
    public String name() {
        return "Cheapest";
    }

    @Override
    protected double score(Route route, DeliveryRequest request) {
        return RouteMetrics.totalCost(route, request);
    }

    @Override
    protected String explanation(Route route, DeliveryRequest request, double score) {
        return "Selected %s: (%.2f base + %.1f km x %.2f + %.2f fees) x %.2f priority = %.2f."
                .formatted(
                        route.id(),
                        route.baseCost(),
                        route.distanceKm(),
                        route.distanceCostPerKm(),
                        route.additionalFees(),
                        1.0 + request.priority().costSurchargeRate(),
                        score
                );
    }
}
