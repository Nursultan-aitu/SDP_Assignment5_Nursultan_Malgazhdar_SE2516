package kz.aitu.routeoptimizer.service;

import kz.aitu.routeoptimizer.domain.DeliveryRequest;
import kz.aitu.routeoptimizer.domain.Route;

import java.util.List;
import java.util.Map;

public record EligibleRoutes(List<Route> routes, Map<String, String> rejectedRoutes) {
}
