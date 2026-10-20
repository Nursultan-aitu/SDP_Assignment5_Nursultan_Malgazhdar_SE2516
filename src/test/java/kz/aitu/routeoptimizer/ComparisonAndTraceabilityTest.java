package kz.aitu.routeoptimizer;

import kz.aitu.routeoptimizer.demo.DemoData;
import kz.aitu.routeoptimizer.domain.DeliveryPriority;
import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.service.RouteDecision;
import kz.aitu.routeoptimizer.service.StrategyComparisonService;
import kz.aitu.routeoptimizer.strategy.BalancedRouteStrategy;
import kz.aitu.routeoptimizer.strategy.CheapestRouteStrategy;
import kz.aitu.routeoptimizer.strategy.EcoRouteStrategy;
import kz.aitu.routeoptimizer.strategy.FastestRouteStrategy;
import kz.aitu.routeoptimizer.strategy.SafestRouteStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComparisonAndTraceabilityTest {

    private final DeliveryRequest request = new DeliveryRequest(
            "REQ", DeliveryPriority.STANDARD, 3_000, 90, false, 10
    );

    @Test
    void comparisonExecutesEveryRegisteredStrategy() {
        Map<String, RouteDecision> decisions = comparison().compare(DemoData.routes(), request);

        assertEquals(List.of("Fastest", "Cheapest", "Eco", "Balanced", "Safest"),
                List.copyOf(decisions.keySet()));
    }

    @Test
    void everyDecisionExplainsItsResult() {
        comparison().compare(DemoData.routes(), request).values().forEach(decision -> {
            assertFalse(decision.explanation().isBlank());
            assertTrue(decision.explanation().contains(decision.selectedRoute().id()));
        });
    }

    @Test
    void everyDecisionReportsRejectedRoutes() {
        comparison().compare(DemoData.routes(), request).values().forEach(decision ->
                assertEquals("route is unavailable", decision.rejectedRoutes().get("R7-Closed"))
        );
    }

    private StrategyComparisonService comparison() {
        return new StrategyComparisonService(List.of(
                new FastestRouteStrategy(),
                new CheapestRouteStrategy(),
                new EcoRouteStrategy(),
                new BalancedRouteStrategy(),
                new SafestRouteStrategy()
        ));
    }
}
