package kz.aitu.routeoptimizer.service;

import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.domain.Route;
import kz.aitu.routeoptimizer.strategy.RouteStrategy;

import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class StrategyComparisonService {

    private final List<RouteStrategy> strategies;

    public StrategyComparisonService(List<RouteStrategy> strategies) {
        Objects.requireNonNull(strategies, "strategies must not be null");
        if (strategies.isEmpty()) {
            throw new IllegalArgumentException("At least one strategy is required");
        }
        this.strategies = List.copyOf(strategies);
    }

    public Map<String, RouteDecision> compare(List<Route> routes, DeliveryRequest request) {
        Map<String, RouteDecision> decisions = new LinkedHashMap<>();
        for (RouteStrategy strategy : strategies) {
            decisions.put(strategy.name(), strategy.selectRoute(routes, request));
        }
        return Collections.unmodifiableMap(decisions);
    }
}
