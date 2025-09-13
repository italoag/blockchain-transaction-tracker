# Project Structure

## Package Organization
The project follows standard Maven directory structure with domain-driven package organization under `dev.bloco.wallet`:

```
src/main/java/dev/bloco/
├── blockchain/          # Shared blockchain utilities
│   └── BlockchainMonitorService.java
└── wallet/             # Main application domain
    ├── WalletApplication.java    # Spring Boot main class
    ├── config/         # Configuration classes
    │   ├── BlockchainNetworkConfig.java
    │   ├── WebFluxConfig.java
    │   └── WebSocketConfig.java
    ├── handler/        # WebSocket and HTTP handlers
    │   ├── TrackingController.java
    │   └── TrackingWebSocketHandler.java
    ├── model/          # Domain models (records preferred)
    │   ├── Block.java
    │   ├── BridgeEvent.java
    │   ├── TrackingRequest.java
    │   └── Transaction.java
    ├── repository/     # Data access layer
    │   ├── BridgeEventRepository.java
    │   ├── InMemoryBridgeEventRepository.java
    │   ├── InMemoryTransactionRepository.java
    │   └── TransactionRepository.java
    ├── service/        # Business logic layer
    │   ├── BlockchainConsumerService.java
    │   ├── BloomFilterService.java
    │   ├── BridgeDetectionService.java
    │   ├── BridgeEventTrackingService.java
    │   └── TransactionTrackingService.java
    └── util/           # Utility classes
        ├── BloomFilterUtil.java
        └── NetworkUtils.java
```

## Architectural Patterns

### Layered Architecture
- **Handler Layer**: WebSocket handlers and REST controllers
- **Service Layer**: Business logic and orchestration
- **Repository Layer**: Data access abstraction
- **Model Layer**: Domain objects using Java records

### Reactive Programming
- All services return `Flux<T>` or `Mono<T>` for reactive streams
- WebSocket handlers use reactive session management
- Repository implementations support reactive operations

### Configuration Management
- `@ConfigurationProperties` for external configuration
- Environment-specific settings via YAML profiles
- Network endpoints configured through environment variables

## Naming Conventions
- **Services**: End with `Service` (e.g., `TransactionTrackingService`)
- **Repositories**: End with `Repository` with interface + implementation pattern
- **Models**: Use Java records for immutable data structures
- **Handlers**: End with `Handler` or `Controller`
- **Configuration**: End with `Config`