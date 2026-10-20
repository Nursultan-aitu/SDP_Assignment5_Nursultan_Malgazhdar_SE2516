package kz.aitu.routeoptimizer;

import kz.aitu.routeoptimizer.demo.DemoData;
import kz.aitu.routeoptimizer.domain.DeliveryPriority;
import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.service.NoSuitableRouteException;
import kz.aitu.routeoptimizer.service.RouteDecision;
import kz.aitu.routeoptimizer.strategy.BalancedRouteStrategy;
import kz.aitu.routeoptimizer.strategy.FastestRouteStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteConstraintTest {

    @Test
    void rejectsUnavailableRouteBeforeScoring() {
        RouteDecision decision = strategy().selectRoute(DemoData.routes(), request(DeliveryPriority.STANDARD, 5_000, 120, 20));

        assertEquals("route is unavailable", decision.rejectedRoutes().get("R7-Closed"));
    }

    @Test
    void rejectsRouteThatExceedsBudget() {
        RouteDecision decision = strategy().selectRoute(DemoData.routes(), request(DeliveryPriority.STANDARD, 1_000, 120, 20));

        assertTrue(decision.rejectedRoutes().get("R1-Express").contains("exceeds budget"));
    }

    @Test
    void rejectsRouteThatMissesDeadline() {
        RouteDecision decision = strategy().selectRoute(DemoData.routes(), request(DeliveryPriority.STANDARD, 5_000, 30, 20));

        assertTrue(decision.rejectedRoutes().get("R2-City").contains("exceeds deadline"));
    }

    @Test
    void rejectsRouteProhibitedForPriority() {
        RouteDecision decision = strategy().selectRoute(DemoData.routes(), request(DeliveryPriority.URGENT, 5_000, 120, 20));

        assertTrue(decision.rejectedRoutes().get("R8-Restricted").contains("prohibited"));
    }

    @Test
    void rejectsRouteThatExceedsEmissionLimit() {
        RouteDecision decision = strategy().selectRoute(DemoData.routes(), request(DeliveryPriority.ECO, 5_000, 120, 2));

        assertTrue(decision.rejectedRoutes().get("R1-Express").contains("emissions"));
    }

    @Test
    void throwsExplicitExceptionWhenNoRouteIsValid() {
        NoSuitableRouteException exception = assertThrows(
                NoSuitableRouteException.class,
                () -> strategy().selectRoute(DemoData.routes(), request(DeliveryPriority.ECONOMY, 100, 120, 20))
        );

        assertEquals(8, exception.rejectedRoutes().size());
        assertTrue(exception.getMessage().contains("No route"));
    }

    @Test
    void throwsExplicitExceptionForEmptyCandidateList() {
        assertThrows(NoSuitableRouteException.class,
                () -> strategy().selectRoute(List.of(), request(DeliveryPriority.STANDARD, 5_000, 120, 20)));
    }

    private BalancedRouteStrategy strategy() {
        return new BalancedRouteStrategy();
    }

    private DeliveryRequest request(
            DeliveryPriority priority,
            double budget,
            int deadline,
            double emissions
    ) {
        return new DeliveryRequest("REQ", priority, budget, deadline, false, emissions);
    }
}
