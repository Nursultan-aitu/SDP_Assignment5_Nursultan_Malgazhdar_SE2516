package kz.aitu.routeoptimizer.strategy;

import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.domain.Route;
import kz.aitu.routeoptimizer.service.RouteMetrics;

public final class SafestRouteStrategy extends AbstractScoredStrategy {

    @Override
    public String name() {
        return "Safest";
    }

    @Override
    protected double score(Route route, DeliveryRequest request) {
        return RouteMetrics.operationalRisk(route);
    }

    @Override
    protected String explanation(Route route, DeliveryRequest request, double score) {
        return "Selected %s: traffic risk %.2f + weather risk %.2f + safety penalty %.2f = %.3f."
                .formatted(
                        route.id(),
                        route.trafficLevel().riskFactor(),
                        route.weatherCondition().riskFactor(),
                        (10.0 - route.safetyRating()) / 10.0,
                        score
                );
    }
}
