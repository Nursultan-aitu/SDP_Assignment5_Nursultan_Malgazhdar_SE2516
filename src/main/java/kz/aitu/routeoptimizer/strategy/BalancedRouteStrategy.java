package kz.aitu.routeoptimizer.strategy;

import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.domain.Route;
import kz.aitu.routeoptimizer.service.EligibleRoutes;
import kz.aitu.routeoptimizer.service.RouteDecision;
import kz.aitu.routeoptimizer.service.RouteEligibility;
import kz.aitu.routeoptimizer.service.RouteMetrics;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

public final class BalancedRouteStrategy implements RouteStrategy {

    private final BalancedWeights weights;

    public BalancedRouteStrategy() {
        this(BalancedWeights.equal());
    }

    public BalancedRouteStrategy(BalancedWeights weights) {
        this.weights = Objects.requireNonNull(weights, "weights must not be null");
    }

    public BalancedWeights weights() {
        return weights;
    }

    @Override
    public String name() {
        return "Balanced";
    }

    @Override
    public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        EligibleRoutes candidates = RouteEligibility.evaluate(routes, request);
        Ranges ranges = Ranges.from(candidates.routes(), request);
        Route selected = candidates.routes().stream()
                .min(Comparator.comparingDouble(route -> score(route, request, ranges)))
                .orElseThrow();

        double time = ranges.time().normalize(RouteMetrics.adjustedMinutes(selected));
        double cost = ranges.cost().normalize(RouteMetrics.totalCost(selected, request));
        double distance = ranges.distance().normalize(selected.distanceKm());
        double emissions = ranges.emissions().normalize(selected.carbonEmissionKg());
        double score = weightedScore(time, cost, distance, emissions);

        String explanation = ("Selected %s: normalized time %.3f x %.2f + cost %.3f x %.2f "
                + "+ distance %.3f x %.2f + emissions %.3f x %.2f = %.3f.")
                .formatted(
                        selected.id(),
                        time, weights.time(),
                        cost, weights.cost(),
                        distance, weights.distance(),
                        emissions, weights.emissions(),
                        score
                );

        return new RouteDecision(
                name(), selected, score, explanation, candidates.rejectedRoutes()
        );
    }

    private double score(Route route, DeliveryRequest request, Ranges ranges) {
        return weightedScore(
                ranges.time().normalize(RouteMetrics.adjustedMinutes(route)),
                ranges.cost().normalize(RouteMetrics.totalCost(route, request)),
                ranges.distance().normalize(route.distanceKm()),
                ranges.emissions().normalize(route.carbonEmissionKg())
        );
    }

    private double weightedScore(double time, double cost, double distance, double emissions) {
        return time * weights.time()
                + cost * weights.cost()
                + distance * weights.distance()
                + emissions * weights.emissions();
    }

    private record Range(double min, double max) {
        double normalize(double value) {
            return max == min ? 0.0 : (value - min) / (max - min);
        }

        static Range from(List<Route> routes, ToDoubleFunction<Route> metric) {
            double min = routes.stream().mapToDouble(metric).min().orElseThrow();
            double max = routes.stream().mapToDouble(metric).max().orElseThrow();
            return new Range(min, max);
        }
    }

    private record Ranges(Range time, Range cost, Range distance, Range emissions) {
        static Ranges from(List<Route> routes, DeliveryRequest request) {
            return new Ranges(
                    Range.from(routes, route -> RouteMetrics.adjustedMinutes(route)),
                    Range.from(routes, route -> RouteMetrics.totalCost(route, request)),
                    Range.from(routes, Route::distanceKm),
                    Range.from(routes, Route::carbonEmissionKg)
            );
        }
    }
}
