package kz.aitu.routeoptimizer.selector;

import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.strategy.RouteStrategy;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public record StrategyRule(
        String description,
        Predicate<DeliveryRequest> condition,
        Supplier<RouteStrategy> strategyFactory
) {

    public StrategyRule {
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(condition, "condition must not be null");
        Objects.requireNonNull(strategyFactory, "strategyFactory must not be null");
    }

    public boolean matches(DeliveryRequest request) {
        return condition.test(request);
    }

    public RouteStrategy createStrategy() {
        return strategyFactory.get();
    }
}
