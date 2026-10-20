package kz.aitu.routeoptimizer;

import kz.aitu.routeoptimizer.domain.DeliveryPriority;
import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.selector.StrategyRule;
import kz.aitu.routeoptimizer.selector.StrategySelector;
import kz.aitu.routeoptimizer.strategy.BalancedRouteStrategy;
import kz.aitu.routeoptimizer.strategy.CheapestRouteStrategy;
import kz.aitu.routeoptimizer.strategy.EcoRouteStrategy;
import kz.aitu.routeoptimizer.strategy.FastestRouteStrategy;
import kz.aitu.routeoptimizer.strategy.SafestRouteStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class StrategySelectorTest {

    private final StrategySelector selector = StrategySelector.defaultSelector();

    @Test
    void selectsFastestForUrgentRequest() {
        assertInstanceOf(FastestRouteStrategy.class, selector.select(request(DeliveryPriority.URGENT, false)));
    }

    @Test
    void selectsCheapestForEconomyRequest() {
        assertInstanceOf(CheapestRouteStrategy.class, selector.select(request(DeliveryPriority.ECONOMY, false)));
    }

    @Test
    void selectsEcoForEcoRequest() {
        assertInstanceOf(EcoRouteStrategy.class, selector.select(request(DeliveryPriority.ECO, false)));
    }

    @Test
    void selectsBalancedForStandardRequest() {
        assertInstanceOf(BalancedRouteStrategy.class, selector.select(request(DeliveryPriority.STANDARD, false)));
    }

    @Test
    void changingEcoPreferenceChangesSelectedStrategy() {
        assertInstanceOf(EcoRouteStrategy.class, selector.select(request(DeliveryPriority.STANDARD, true)));
    }

    @Test
    void acceptsNewRulesWithoutChangingSelectorClass() {
        StrategySelector custom = new StrategySelector(List.of(
                new StrategyRule("always safest", ignored -> true, SafestRouteStrategy::new)
        ));

        assertInstanceOf(SafestRouteStrategy.class, custom.select(request(DeliveryPriority.STANDARD, false)));
    }

    private DeliveryRequest request(DeliveryPriority priority, boolean ecoPreferred) {
        return new DeliveryRequest("REQ", priority, 5_000, 120, ecoPreferred, 20);
    }
}
