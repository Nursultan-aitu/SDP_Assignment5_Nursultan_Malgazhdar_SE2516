package kz.aitu.routeoptimizer.strategy;

import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.domain.Route;
import kz.aitu.routeoptimizer.service.RouteMetrics;

public final class FastestRouteStrategy extends AbstractScoredStrategy {

    @Override
    public String name() {
        return "Fastest";
    }

    @Override
    protected double score(Route route, DeliveryRequest request) {
        return RouteMetrics.adjustedMinutes(route);
    }

    @Override
    protected String explanation(Route route, DeliveryRequest request, double score) {
        return "Selected %s: %d base min + %d traffic min + %d weather min = %.0f min."
                .formatted(
                        route.id(),
                        route.estimatedMinutes(),
                        route.trafficLevel().delayMinutes(),
                        route.weatherCondition().delayMinutes(),
                        score
                );
    }
}
