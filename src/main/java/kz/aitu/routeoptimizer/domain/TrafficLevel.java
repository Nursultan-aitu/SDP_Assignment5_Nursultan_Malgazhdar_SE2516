package kz.aitu.routeoptimizer.domain;

public enum TrafficLevel {
    LOW(0, 0.10),
    NORMAL(4, 0.25),
    HIGH(12, 0.60),
    SEVERE(25, 1.00);

    private final int delayMinutes;
    private final double riskFactor;

    TrafficLevel(int delayMinutes, double riskFactor) {
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

