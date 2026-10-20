package kz.aitu.routeoptimizer.domain;

public enum DeliveryPriority {
    URGENT(0.25),
    ECONOMY(0.00),
    ECO(0.05),
    STANDARD(0.10);

    private final double costSurchargeRate;

    DeliveryPriority(double costSurchargeRate) {
        this.costSurchargeRate = costSurchargeRate;
    }

    public double costSurchargeRate() {
        return costSurchargeRate;
    }
}

