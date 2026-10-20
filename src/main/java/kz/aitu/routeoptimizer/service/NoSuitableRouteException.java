package kz.aitu.routeoptimizer.service;

import java.util.LinkedHashMap;
import java.util.Map;

public class NoSuitableRouteException extends RuntimeException {

    private final Map<String, String> rejectedRoutes;

    public NoSuitableRouteException(String message, Map<String, String> rejectedRoutes) {
        super(message);
        this.rejectedRoutes = Map.copyOf(new LinkedHashMap<>(rejectedRoutes));
    }

    public Map<String, String> rejectedRoutes() {
        return rejectedRoutes;
    }
}
