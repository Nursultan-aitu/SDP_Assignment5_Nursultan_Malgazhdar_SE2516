package kz.aitu.routeoptimizer.strategy;

import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.domain.Route;
import kz.aitu.routeoptimizer.service.RouteDecision;

import java.util.List;

public interface RouteStrategy {

    String name();

    RouteDecision selectRoute(List<Route> routes, DeliveryRequest request);
}
