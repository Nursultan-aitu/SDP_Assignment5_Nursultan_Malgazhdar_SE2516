package kz.aitu.routeoptimizer.demo;

import kz.aitu.routeoptimizer.domain.DeliveryPriority;
import kz.aitu.routeoptimizer.domain.Location;
import kz.aitu.routeoptimizer.domain.Route;
import kz.aitu.routeoptimizer.domain.TrafficLevel;
import kz.aitu.routeoptimizer.domain.WeatherCondition;

import java.util.List;
import java.util.Set;

public final class DemoData {

    private DemoData() {
    }

    public static List<Route> routes() {
        Location warehouse = new Location("Almaty Warehouse", 43.2389, 76.8897);
        Location customer = new Location("Customer in Medeu", 43.1575, 77.0588);

        return List.of(
                route("R1-Express", warehouse, customer, 18, 24, 900, 40, 300,
                        5.5, TrafficLevel.LOW, WeatherCondition.CLEAR, 9.2, true, Set.of()),
                route("R2-City", warehouse, customer, 14, 30, 500, 28, 100,
                        3.8, TrafficLevel.HIGH, WeatherCondition.RAIN, 8.0, true, Set.of()),
                route("R3-Eco", warehouse, customer, 20, 38, 450, 25, 50,
                        1.2, TrafficLevel.NORMAL, WeatherCondition.CLEAR, 9.0, true, Set.of()),
                route("R4-Highway", warehouse, customer, 23, 22, 1100, 45, 350,
                        7.5, TrafficLevel.NORMAL, WeatherCondition.RAIN, 9.5, true, Set.of()),
                route("R5-Budget", warehouse, customer, 17, 42, 300, 20, 0,
                        4.5, TrafficLevel.HIGH, WeatherCondition.CLEAR, 7.8, true, Set.of()),
                route("R6-Scenic", warehouse, customer, 26, 45, 600, 24, 80,
                        1.8, TrafficLevel.LOW, WeatherCondition.CLEAR, 9.6, true, Set.of()),
                route("R7-Closed", warehouse, customer, 12, 18, 350, 25, 50,
                        3.0, TrafficLevel.LOW, WeatherCondition.CLEAR, 8.5, false, Set.of()),
                route("R8-Restricted", warehouse, customer, 15, 27, 420, 26, 80,
                        2.8, TrafficLevel.NORMAL, WeatherCondition.CLEAR, 8.8, true,
                        Set.of(DeliveryPriority.URGENT))
        );
    }

    private static Route route(
            String id,
            Location origin,
            Location destination,
            double distance,
            int minutes,
            double baseCost,
            double costPerKm,
            double fees,
            double emissions,
            TrafficLevel traffic,
            WeatherCondition weather,
            double safety,
            boolean available,
            Set<DeliveryPriority> prohibitedPriorities
    ) {
        return new Route(
                id, origin, destination, distance, minutes, baseCost, costPerKm, fees,
                emissions, traffic, weather, safety, available, prohibitedPriorities
        );
    }
}
