package kz.aitu.routeoptimizer.selector;

import kz.aitu.routeoptimizer.domain.DeliveryPriority;
import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.strategy.BalancedRouteStrategy;
import kz.aitu.routeoptimizer.strategy.CheapestRouteStrategy;
import kz.aitu.routeoptimizer.strategy.EcoRouteStrategy;
import kz.aitu.routeoptimizer.strategy.FastestRouteStrategy;
import kz.aitu.routeoptimizer.strategy.RouteStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class StrategySelector {

    private final List<StrategyRule> rules;

    public StrategySelector(List<StrategyRule> rules) {
        Objects.requireNonNull(rules, "rules must not be null");
        if (rules.isEmpty()) {
            throw new IllegalArgumentException("At least one strategy rule is required");
        }
        this.rules = List.copyOf(new ArrayList<>(rules));
    }

    public static StrategySelector defaultSelector() {
        return new StrategySelector(List.of(
                new StrategyRule(
                        "Urgent deliveries use the fastest route",
                        request -> request.priority() == DeliveryPriority.URGENT,
                        FastestRouteStrategy::new
                ),
                new StrategyRule(
                        "Economy deliveries minimize cost",
                        request -> request.priority() == DeliveryPriority.ECONOMY,
                        CheapestRouteStrategy::new
                ),
                new StrategyRule(
                        "Eco deliveries and eco preference minimize environmental impact",
                        request -> request.priority() == DeliveryPriority.ECO || request.ecoPreferred(),
                        EcoRouteStrategy::new
                ),
                new StrategyRule(
                        "Other deliveries use balanced optimization",
                        request -> true,
                        BalancedRouteStrategy::new
                )
        ));
    }

    public RouteStrategy select(DeliveryRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return rules.stream()
                .filter(rule -> rule.matches(request))
                .findFirst()
                .map(StrategyRule::createStrategy)
                .orElseThrow(() -> new IllegalStateException("No strategy rule matched the request"));
    }

    public List<StrategyRule> rules() {
        return rules;
    }
}
