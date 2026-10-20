package kz.aitu.routeoptimizer;

import kz.aitu.routeoptimizer.demo.DemoData;
import kz.aitu.routeoptimizer.domain.DeliveryPriority;
import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.domain.Location;
import kz.aitu.routeoptimizer.domain.Route;
import kz.aitu.routeoptimizer.domain.TrafficLevel;
import kz.aitu.routeoptimizer.domain.WeatherCondition;
import kz.aitu.routeoptimizer.service.RouteDecision;
import kz.aitu.routeoptimizer.service.RoutePlanner;
import kz.aitu.routeoptimizer.strategy.BalancedRouteStrategy;
import kz.aitu.routeoptimizer.strategy.BalancedWeights;
import kz.aitu.routeoptimizer.strategy.CheapestRouteStrategy;
import kz.aitu.routeoptimizer.strategy.EcoRouteStrategy;
import kz.aitu.routeoptimizer.strategy.FastestRouteStrategy;
import kz.aitu.routeoptimizer.strategy.SafestRouteStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteStrategyTest {

    private final DeliveryRequest standard = new DeliveryRequest(
            "REQ", DeliveryPriority.STANDARD, 3_000, 90, false, 10
    );

    @Test
    void fastestUsesTrafficAndWeatherInsteadOfBaseTimeOnly() {
        Location from = new Location("From", 43.2, 76.8);
        Location to = new Location("To", 43.3, 76.9);
        Route delayed = route("base-fast", from, to, 10, 10,
                TrafficLevel.SEVERE, WeatherCondition.STORM);
        Route reliable = route("reliable", from, to, 12, 20,
                TrafficLevel.LOW, WeatherCondition.CLEAR);

        RouteDecision decision = new FastestRouteStrategy()
                .selectRoute(List.of(delayed, reliable), standard);

        assertEquals("reliable", decision.selectedRoute().id());
        assertEquals(20.0, decision.score());
    }

    @Test
    void cheapestUsesDocumentedCostFormula() {
        RouteDecision decision = new CheapestRouteStrategy().selectRoute(DemoData.routes(), standard);

        assertEquals("R5-Budget", decision.selectedRoute().id());
        assertEquals(704.0, decision.score(), 0.001);
        assertTrue(decision.explanation().contains("base"));
    }

    @Test
    void ecoUsesEmissionsAndDistance() {
        RouteDecision decision = new EcoRouteStrategy().selectRoute(DemoData.routes(), standard);

        assertEquals("R8-Restricted", decision.selectedRoute().id());
        assertEquals(5.85, decision.score(), 0.001);
    }

    @Test
    void balancedNormalizesFourMetrics() {
        RouteDecision decision = new BalancedRouteStrategy().selectRoute(DemoData.routes(), standard);

        assertEquals("R8-Restricted", decision.selectedRoute().id());
        assertTrue(decision.explanation().contains("normalized time"));
        assertTrue(decision.explanation().contains("emissions"));
    }

    @Test
    void balancedWeightsCanPrioritizeTime() {
        BalancedRouteStrategy strategy = new BalancedRouteStrategy(
                new BalancedWeights(1.0, 0.0, 0.0, 0.0)
        );

        assertEquals("R1-Express", strategy.selectRoute(DemoData.routes(), standard).selectedRoute().id());
    }

    @Test
    void rejectsBalancedWeightsThatDoNotSumToOne() {
        assertThrows(IllegalArgumentException.class,
                () -> new BalancedWeights(0.5, 0.5, 0.5, 0.5));
    }

    @Test
    void safestCombinesTrafficWeatherAndSafetyRating() {
        RouteDecision decision = new SafestRouteStrategy().selectRoute(DemoData.routes(), standard);

        assertEquals("R6-Scenic", decision.selectedRoute().id());
        assertTrue(decision.explanation().contains("safety penalty"));
    }

    @Test
    void routePlannerSwitchesStrategyAtRuntime() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        assertEquals("R1-Express", planner.plan(DemoData.routes(), standard).selectedRoute().id());

        planner.setStrategy(new CheapestRouteStrategy());
        assertEquals("R5-Budget", planner.plan(DemoData.routes(), standard).selectedRoute().id());

        planner.setStrategy(new EcoRouteStrategy());
        assertEquals("R8-Restricted", planner.plan(DemoData.routes(), standard).selectedRoute().id());
    }

    private Route route(
            String id,
            Location from,
            Location to,
            double distance,
            int minutes,
            TrafficLevel traffic,
            WeatherCondition weather
    ) {
        return new Route(id, from, to, distance, minutes, 100, 10, 0,
                1, traffic, weather, 9, true, Set.of());
    }
}
