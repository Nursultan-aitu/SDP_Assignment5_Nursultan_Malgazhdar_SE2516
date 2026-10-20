# Smart Delivery and Route Optimization Engine

Assignment 5 demonstrates the Strategy design pattern in a delivery route optimizer. The application evaluates eight candidate routes, rejects routes that violate hard constraints, and then selects the best remaining route with a replaceable optimization strategy.

## Requirements

- Java 17 or newer
- Maven 3.9+
- JUnit 5 (downloaded by Maven)

## Run the project

```bash
mvn clean test
mvn package
java -cp target/classes kz.aitu.routeoptimizer.RouteOptimizerDemo
```

In IntelliJ IDEA, open `pom.xml` as a Maven project and run `RouteOptimizerDemo.main()`.

## Implemented strategies

| Strategy | Optimization rule | Typical request |
|---|---|---|
| `FastestRouteStrategy` | Minimum base time plus traffic and weather delay | `URGENT` |
| `CheapestRouteStrategy` | Minimum `(base + distance × rate + fees) × priority multiplier` | `ECONOMY` |
| `EcoRouteStrategy` | Minimum `emissions × 0.75 + distance × 0.25` | `ECO` or eco preference |
| `BalancedRouteStrategy` | Minimum weighted normalized time, cost, distance, and emissions | `STANDARD` |
| `SafestRouteStrategy` | Minimum traffic risk, weather risk, and safety penalty | Optional extension |

The default balanced weights are `0.25` for every metric. Custom values are accepted through `BalancedWeights`, but negative weights and a total other than `1.0` are rejected.

## Hard constraints

`RouteEligibility` checks every route before a strategy calculates its score. A route is rejected when it is:

- unavailable;
- prohibited for the request priority;
- over the maximum budget;
- slower than the delivery deadline after traffic and weather delays;
- above the emission limit.

The decision contains the rejection reason for every excluded route. If no route remains, `NoSuitableRouteException` is thrown instead of returning `null`.

## Automatic selection

`StrategySelector` stores an ordered collection of `StrategyRule` objects:

```text
URGENT  -> Fastest
ECONOMY -> Cheapest
ECO     -> Eco
STANDARD -> Balanced
ecoPreferred = true -> Eco (unless a higher-priority rule matched)
```

This is not implemented as a large `switch`. A new selection rule can be registered by constructing another `StrategySelector` with an additional predicate and strategy factory.

## Architecture and SOLID

`RouteStrategy` is the strategy hierarchy. Its concrete implementations are Fastest, Cheapest, Eco, Balanced, and Safest. `RoutePlanner` is the context: it stores only a `RouteStrategy` reference and delegates route selection to it.

- **SRP:** domain data, eligibility checks, metric calculations, selection rules, strategies, and comparison are separate classes.
- **OCP:** a new algorithm implements `RouteStrategy`; the planner does not change. `SafestRouteStrategy` was added this way after the initial application.
- **DIP:** `RoutePlanner` and `StrategyComparisonService` depend on the `RouteStrategy` interface, not concrete algorithms.
- **Encapsulation:** route fields and request limits are immutable records; balanced weights validate their invariant at construction time.
- **Polymorphism:** all algorithms are invoked through the same `selectRoute` contract.

## UML diagrams

- [Class diagram](docs/class-diagram.puml)
- [Runtime selection sequence](docs/sequence-diagram.puml)

The source is PlantUML and can be previewed by the PlantUML IntelliJ plugin or rendered by any PlantUML server.

## Comparison mode

`StrategyComparisonService` runs every registered strategy against the same candidate list and request. Its result preserves strategy order and reports each selected route, numeric score, explanation, and rejected routes. Because the strategies use different units, raw scores are not compared with each other; the table is intended to explain the decision made by each algorithm.

Example output:

```text
[Fastest] R1-Express | score 24.000
[Cheapest] R5-Budget | score 704.000
[Eco] R8-Restricted | score 5.850
[Balanced] R8-Restricted | score 0.177
[Safest] R6-Scenic | score 0.190
```

The demo also shows three failure cases: no route within budget, no route before the deadline, and no route under the emission limit.

## Tests

The project contains 24 JUnit 5 tests covering:

- all five strategy results and formulas;
- traffic and weather in fastest-route calculation;
- valid and invalid balanced weights;
- runtime switching through the context;
- the four default automatic-selection rules and dynamic eco preference;
- adding a custom selector rule without changing `StrategySelector`;
- unavailable, over-budget, late, prohibited, and high-emission routes;
- explicit failure when no valid route exists;
- comparison mode, explanations, and rejected-route traceability.

Run them with `mvn test`.

## Design questions

### Which classes form the strategy hierarchy?

`RouteStrategy` is the interface. `FastestRouteStrategy`, `CheapestRouteStrategy`, `EcoRouteStrategy`, `BalancedRouteStrategy`, and `SafestRouteStrategy` are concrete strategies. Four of them share the small `AbstractScoredStrategy` template; Balanced implements the interface directly because normalization needs the complete candidate set.

### How does the context avoid depending on concrete strategies?

`RoutePlanner` accepts and stores a `RouteStrategy`. It has no imports or conditions for Fastest, Cheapest, Eco, Balanced, or Safest. The caller selects an implementation and may replace it at runtime with `setStrategy`.

### What changes when another strategy is added?

Only a new class implementing `RouteStrategy` is required. It can then be registered in the comparison list or in a `StrategyRule`. Existing planner and strategy classes remain unchanged.

### How are strategies compared on the same data?

`StrategyComparisonService.compare` receives one immutable request and one route list, then invokes each registered strategy with those same arguments. Every result is stored by strategy name.

### Why is this Strategy rather than Bridge?

Strategy represents interchangeable algorithms for one responsibility: choosing a route. The application swaps that algorithm according to request conditions. Bridge would model two independent abstraction hierarchies that must vary separately, which is not the main problem here.

### What is one real limitation of this design?

The route data is an in-memory snapshot. Traffic, weather, prices, and road closures do not update from external services, so a decision may become stale after it is calculated. A production version would load live data and would need rules for timeouts, caching, and partial provider failures.
