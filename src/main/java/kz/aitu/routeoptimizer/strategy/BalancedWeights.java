package kz.aitu.routeoptimizer.strategy;

public record BalancedWeights(
        double time,
        double cost,
        double distance,
        double emissions
) {

    private static final double EPSILON = 0.000_001;

    public BalancedWeights {
        if (time < 0.0 || cost < 0.0 || distance < 0.0 || emissions < 0.0) {
            throw new IllegalArgumentException("Balanced weights must not be negative");
        }
        double sum = time + cost + distance + emissions;
        if (Math.abs(sum - 1.0) > EPSILON) {
            throw new IllegalArgumentException("Balanced weights must add up to 1.0");
        }
    }

    public static BalancedWeights equal() {
        return new BalancedWeights(0.25, 0.25, 0.25, 0.25);
    }
}
