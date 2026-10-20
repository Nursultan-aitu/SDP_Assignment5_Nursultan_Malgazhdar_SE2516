package kz.aitu.routeoptimizer.domain;

public enum WeatherCondition {
    CLEAR(0, 0.05),
    RAIN(3, 0.30),
    SNOW(8, 0.65),
    STORM(20, 1.00);

    private final int delayMinutes;
    private final double riskFactor;

    WeatherCondition(int delayMinutes, double riskFactor) {
        this.delayMinutes = delayMinutes;
        this.riskFactor = riskFactor;
    }

    public int delayMinutes() {
        return delayMinutes;
    }

    public double riskFactor() {
        return riskFactor;
    }
}

