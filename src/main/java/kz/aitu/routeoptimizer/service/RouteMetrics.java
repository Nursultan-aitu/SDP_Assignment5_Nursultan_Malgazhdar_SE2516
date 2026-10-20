package kz.aitu.routeoptimizer.service;

import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.domain.Route;

public final class RouteMetrics {

    private RouteMetrics() {
    }

    public static int adjustedMinutes(Route route) {
        return route.estimatedMinutes()
                + route.trafficLevel().delayMinutes()
                + route.weatherCondition().delayMinutes();
    }

    public static double totalCost(Route route, DeliveryRequest request) {
        double subtotal = route.baseCost()
                + route.distanceKm() * route.distanceCostPerKm()
                + route.additionalFees();
        return subtotal * (1.0 + request.priority().costSurchargeRate());
    }

    public static double operationalRisk(Route route) {
        return route.trafficLevel().riskFactor()
                + route.weatherCondition().riskFactor()
                + (10.0 - route.safetyRating()) / 10.0;
    }
}
