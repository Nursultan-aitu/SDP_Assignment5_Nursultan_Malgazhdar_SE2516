package kz.aitu.routeoptimizer.service;

import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.domain.Route;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RouteEligibility {

    private RouteEligibility() {
    }

    public static EligibleRoutes evaluate(List<Route> routes, DeliveryRequest request) {
        Objects.requireNonNull(routes, "routes must not be null");
        Objects.requireNonNull(request, "request must not be null");

        List<Route> eligible = new ArrayList<>();
        Map<String, String> rejected = new LinkedHashMap<>();

        for (Route route : routes) {
            String reason = rejectionReason(route, request);
            if (reason == null) {
                eligible.add(route);
            } else {
                rejected.put(route.id(), reason);
            }
        }

        if (eligible.isEmpty()) {
            throw new NoSuitableRouteException(
                    "No route satisfies all hard constraints for request " + request.requestId(),
                    rejected
            );
        }
        return new EligibleRoutes(List.copyOf(eligible), rejected);
    }

    private static String rejectionReason(Route route, DeliveryRequest request) {
        if (!route.available()) {
            return "route is unavailable";
        }
        if (route.prohibitedPriorities().contains(request.priority())) {
            return "route is prohibited for priority " + request.priority();
        }

        double cost = RouteMetrics.totalCost(route, request);
        if (cost > request.maxBudget()) {
            return "cost %.2f exceeds budget %.2f".formatted(cost, request.maxBudget());
        }

        int minutes = RouteMetrics.adjustedMinutes(route);
        if (minutes > request.maxDeliveryMinutes()) {
            return "delivery time %d min exceeds deadline %d min"
                    .formatted(minutes, request.maxDeliveryMinutes());
        }

        if (route.carbonEmissionKg() > request.maxEmissionKg()) {
            return "emissions %.2f kg exceed limit %.2f kg"
                    .formatted(route.carbonEmissionKg(), request.maxEmissionKg());
        }
        return null;
    }
}
