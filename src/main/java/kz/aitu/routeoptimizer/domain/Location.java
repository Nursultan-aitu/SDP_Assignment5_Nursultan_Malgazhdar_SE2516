package kz.aitu.routeoptimizer.domain;

import java.util.Objects;

public record Location(String name, double latitude, double longitude) {

    public Location {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        }
    }
}

