package kz.aitu.routeoptimizer.strategy;

import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.domain.Route;
import kz.aitu.routeoptimizer.service.EligibleRoutes;
import kz.aitu.routeoptimizer.service.RouteDecision;
import kz.aitu.routeoptimizer.service.RouteEligibility;

import java.util.Comparator;
import java.util.List;

abstract class AbstractScoredStrategy implements RouteStrategy {

    @Override
    public final RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        EligibleRoutes candidates = RouteEligibility.evaluate(routes, request);
        Route selected = candidates.routes().stream()
                .min(Comparator.comparingDouble(route -> score(route, request)))
                .orElseThrow();
        double score = score(selected, request);
        return new RouteDecision(
                name(),
                selected,
                score,
                explanation(selected, request, score),
                candidates.rejectedRoutes()
        );
    }

    protected abstract double score(Route route, DeliveryRequest request);

    protected abstract String explanation(Route route, DeliveryRequest request, double score);
}
