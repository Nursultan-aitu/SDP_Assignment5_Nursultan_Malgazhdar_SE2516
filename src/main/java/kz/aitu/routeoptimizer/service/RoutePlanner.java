package kz.aitu.routeoptimizer.service;

import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.domain.Route;
import kz.aitu.routeoptimizer.strategy.RouteStrategy;

import java.util.List;
import java.util.Objects;

public final class RoutePlanner {

    private RouteStrategy strategy;

    public RoutePlanner(RouteStrategy strategy) {
        setStrategy(strategy);
    }

    public void setStrategy(RouteStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "strategy must not be null");
    }

    public String currentStrategyName() {
        return strategy.name();
    }

    public RouteDecision plan(List<Route> routes, DeliveryRequest request) {
        return strategy.selectRoute(routes, request);
    }
}
