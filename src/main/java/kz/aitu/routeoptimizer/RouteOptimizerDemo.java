package kz.aitu.routeoptimizer;

import kz.aitu.routeoptimizer.demo.DemoData;
import kz.aitu.routeoptimizer.domain.DeliveryPriority;
import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.domain.Route;
import kz.aitu.routeoptimizer.selector.StrategySelector;
import kz.aitu.routeoptimizer.service.NoSuitableRouteException;
import kz.aitu.routeoptimizer.service.RouteDecision;
import kz.aitu.routeoptimizer.service.RoutePlanner;
import kz.aitu.routeoptimizer.service.StrategyComparisonService;
import kz.aitu.routeoptimizer.strategy.BalancedRouteStrategy;
import kz.aitu.routeoptimizer.strategy.CheapestRouteStrategy;
import kz.aitu.routeoptimizer.strategy.EcoRouteStrategy;
import kz.aitu.routeoptimizer.strategy.FastestRouteStrategy;
import kz.aitu.routeoptimizer.strategy.RouteStrategy;
import kz.aitu.routeoptimizer.strategy.SafestRouteStrategy;

import java.util.List;
import java.util.Map;

public final class RouteOptimizerDemo {

    private RouteOptimizerDemo() {
    }

    public static void main(String[] args) {
        List<Route> routes = DemoData.routes();
        DeliveryRequest standard = request("REQ-STANDARD", DeliveryPriority.STANDARD,
                3_000, 90, false, 10);

        demonstrateRuntimeSwitching(routes, standard);
        demonstrateAutomaticSelection(routes);
        demonstrateDynamicRequest(routes);
        demonstrateComparison(routes, standard);
        demonstrateFailures(routes);
    }

    private static void demonstrateRuntimeSwitching(List<Route> routes, DeliveryRequest request) {
        System.out.println("=== Runtime strategy switching ===");
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        printDecision(planner.plan(routes, request));

        planner.setStrategy(new CheapestRouteStrategy());
        printDecision(planner.plan(routes, request));

        planner.setStrategy(new EcoRouteStrategy());
        printDecision(planner.plan(routes, request));

        planner.setStrategy(new BalancedRouteStrategy());
        printDecision(planner.plan(routes, request));

        planner.setStrategy(new SafestRouteStrategy());
        printDecision(planner.plan(routes, request));
    }

    private static void demonstrateAutomaticSelection(List<Route> routes) {
        System.out.println("\n=== Automatic strategy selection ===");
        StrategySelector selector = StrategySelector.defaultSelector();
        for (DeliveryPriority priority : DeliveryPriority.values()) {
            DeliveryRequest request = request(
                    "REQ-" + priority, priority, 3_000, 90, false, 10
            );
            RouteStrategy strategy = selector.select(request);
            System.out.printf("%s -> %s%n", priority, strategy.name());
            printDecision(strategy.selectRoute(routes, request));
        }
    }

    private static void demonstrateDynamicRequest(List<Route> routes) {
        System.out.println("\n=== Request change triggers a new strategy ===");
        StrategySelector selector = StrategySelector.defaultSelector();
        DeliveryRequest original = request(
                "REQ-DYNAMIC", DeliveryPriority.STANDARD, 3_000, 90, false, 10
        );
        DeliveryRequest changed = request(
                "REQ-DYNAMIC", DeliveryPriority.STANDARD, 3_000, 90, true, 10
        );
        System.out.println("Before eco preference: " + selector.select(original).name());
        System.out.println("After eco preference:  " + selector.select(changed).name());
        printDecision(selector.select(changed).selectRoute(routes, changed));
    }

    private static void demonstrateComparison(List<Route> routes, DeliveryRequest request) {
        System.out.println("\n=== Strategy comparison ===");
        StrategyComparisonService comparison = new StrategyComparisonService(List.of(
                new FastestRouteStrategy(),
                new CheapestRouteStrategy(),
                new EcoRouteStrategy(),
                new BalancedRouteStrategy(),
                new SafestRouteStrategy()
        ));
        comparison.compare(routes, request).values().forEach(RouteOptimizerDemo::printDecision);
    }

    private static void demonstrateFailures(List<Route> routes) {
        System.out.println("\n=== Failure cases ===");
        List<DeliveryRequest> impossibleRequests = List.of(
                request("FAIL-BUDGET", DeliveryPriority.ECONOMY, 100, 120, false, 20),
                request("FAIL-DEADLINE", DeliveryPriority.URGENT, 10_000, 5, false, 20),
                request("FAIL-EMISSIONS", DeliveryPriority.ECO, 10_000, 120, true, 0.1)
        );

        for (DeliveryRequest request : impossibleRequests) {
            try {
                new BalancedRouteStrategy().selectRoute(routes, request);
            } catch (NoSuitableRouteException exception) {
                System.out.printf("%s: %s (%d rejected routes)%n",
                        request.requestId(), exception.getMessage(), exception.rejectedRoutes().size());
            }
        }
    }

    private static DeliveryRequest request(
            String id,
            DeliveryPriority priority,
            double budget,
            int deadline,
            boolean ecoPreferred,
            double emissionLimit
    ) {
        return new DeliveryRequest(id, priority, budget, deadline, ecoPreferred, emissionLimit);
    }

    private static void printDecision(RouteDecision decision) {
        System.out.printf("[%s] %s | score %.3f%n",
                decision.strategyName(), decision.selectedRoute().id(), decision.score());
        System.out.println("  " + decision.explanation());
        for (Map.Entry<String, String> rejection : decision.rejectedRoutes().entrySet()) {
            System.out.printf("  rejected %s: %s%n", rejection.getKey(), rejection.getValue());
        }
    }
}
