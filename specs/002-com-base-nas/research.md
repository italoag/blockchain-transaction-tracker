# Research Findings: Blockchain Monitoring Service

**Date**: September 12, 2025
**Feature**: 002-com-base-nas
**Research Focus**: Reactive Web3j integration, resilience patterns, deduplication strategies

## Research Questions & Findings

### 1. Reactive Web3j Integration Patterns

**Decision**: Use `Mono.fromFuture(web3j.method().sendAsync())` with Project Reactor
**Rationale**:
- Web3j's async methods return `CompletableFuture<T>`
- Project Reactor provides seamless conversion with `Mono.fromFuture()`
- Enables reactive stream composition and error handling
- Maintains non-blocking execution throughout the application

**Alternatives Considered**:
- Blocking calls with `.send()` - Rejected due to thread blocking
- Custom Future-to-Flux conversion - Rejected due to complexity vs minimal benefit
- RxJava instead of Reactor - Rejected due to Spring ecosystem integration

**Implementation Pattern**:
```java
@Retry(name = "web3")
@CircuitBreaker(name = "web3")
public Mono<BigInteger> latestBlockNumber(String httpUrl) {
    return Mono.fromFuture(httpClient(httpUrl).ethBlockNumber().sendAsync())
               .map(EthBlockNumber::getBlockNumber);
}
```

### 2. Blockchain Provider Failover Strategies

**Decision**: Multi-provider configuration with Resilience4j Circuit Breaker + Retry
**Rationale**:
- Blockchain providers can have downtime or rate limits
- Circuit breaker prevents cascade failures
- Retry with exponential backoff handles transient failures
- Multiple providers ensure high availability

**Alternatives Considered**:
- Single provider with timeout - Rejected due to single point of failure
- Load balancer approach - Rejected due to provider-specific authentication
- Client-side provider selection - Rejected due to added complexity

**Configuration Pattern**:
```yaml
resilience4j:
  retry:
    instances:
      web3:
        max-attempts: 3
        wait-duration: 1s
  circuitbreaker:
    instances:
      web3:
        sliding-window-type: count_based
        sliding-window-size: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 5s
```

### 3. Bloom Filter Implementation for High-Throughput Deduplication

**Decision**: Custom BitSet-based Bloom filter with multiple hash functions
**Rationale**:
- High throughput requirements (thousands of tx/minute)
- Memory efficient for large datasets
- Thread-safe concurrent access
- Composite key support (network + identifier)

**Alternatives Considered**:
- Guava BloomFilter - Rejected due to blocking API and serialization limitations
- Redis-based deduplication - Rejected due to network overhead
- Database unique constraints - Rejected due to performance impact

**Implementation Pattern**:
```java
public synchronized boolean mightContain(String network, String address) {
    String key = network + address;
    for (int seed : SEEDS) {
        if (!bitSet.get(indexFor(key, seed))) return false;
    }
    return true;
}
```

### 4. WebSocket Connection Management for Blockchain Networks

**Decision**: Persistent WebSocket connections with automatic reconnection
**Rationale**:
- Real-time requirements demand persistent connections
- Automatic reconnection handles network interruptions
- Connection pooling for multiple networks
- Graceful degradation to HTTP polling as fallback

**Alternatives Considered**:
- HTTP polling only - Rejected due to latency and resource usage
- Short-lived connections - Rejected due to connection overhead
- Single connection multiplexing - Rejected due to provider limitations

**Implementation Pattern**:
```java
private Web3j wsClient(String url) {
    return wsClients.computeIfAbsent(url, u -> {
        WebSocketService service = new WebSocketService(u, true);
        try {
            service.connect();
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to websocket at " + u, e);
        }
        return Web3j.build(service);
    });
}
```

## Technical Architecture Decisions

### Reactive Stream Processing
**Decision**: Project Reactor (Flux/Mono) for all async operations
**Rationale**: Native Spring WebFlux integration, excellent error handling, composable operators

### Error Handling Strategy
**Decision**: Resilience4j annotations with custom exception handling
**Rationale**: Declarative configuration, circuit breaker patterns, retry with backoff

### Data Model Design
**Decision**: Java records for immutable data structures
**Rationale**: Thread-safe, concise syntax, automatic equals/hashCode/toString

### Testing Strategy
**Decision**: Integration tests with real Web3j providers, unit tests with mocked dependencies
**Rationale**: Validates end-to-end functionality, fast unit test execution

## Performance Considerations

### Memory Management
- Bloom filter size: 16M bits (configurable)
- Connection pooling: ConcurrentHashMap for Web3j clients
- Reactive streams: Backpressure handling for high-volume scenarios

### Scalability
- Horizontal scaling: Stateless service design
- Concurrent processing: Reactive operators handle parallelism
- Resource limits: Circuit breakers prevent resource exhaustion

## Security Considerations

### Provider Authentication
- Environment variables for API keys
- Secure key management (not hardcoded)
- Provider-specific authentication patterns

### Data Validation
- Input sanitization for addresses/contracts
- Rate limiting for API endpoints
- Secure WebSocket connections (WSS)

## Deployment Considerations

### Container Strategy
- Docker with Eclipse Temurin JRE 21
- Environment-based configuration
- Health checks via Spring Boot Actuator

### Monitoring
- Structured logging with SLF4J
- Metrics collection with Micrometer
- Health endpoints for load balancer integration

## Risk Assessment

### High-Risk Areas
1. **Provider Reliability**: Mitigated by multi-provider support and circuit breakers
2. **High Transaction Volume**: Mitigated by reactive processing and efficient deduplication
3. **Network Connectivity**: Mitigated by automatic reconnection and fallback strategies

### Success Metrics
- Transaction processing latency < 100ms
- 99.9% uptime for monitoring streams
- False positive rate < 0.01% for deduplication
- Memory usage < 512MB under normal load

## Conclusion

The research confirms the technical approach is sound and addresses all identified requirements. The combination of reactive programming, resilience patterns, and efficient data structures provides a robust foundation for real-time blockchain monitoring.

**Next Steps**: Proceed to Phase 1 (Design & Contracts) with confidence in the technical decisions.