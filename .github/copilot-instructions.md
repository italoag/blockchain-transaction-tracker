# Blockchain Transaction Tracker - AI Agent Instructions

## Project Overview
Spring Boot WebFlux microservice for real-time blockchain transaction tracking using Web3j. Features REST API, WebSocket streaming, and custom Bloom filter deduplication.

## Architecture
- **blockchain package**: Web3j integration, HTTP/WebSocket clients
- **wallet package**: Business logic, reactive services, data models
- Reactive streams with Project Reactor (`Flux`, `Mono`)
- Repository pattern with in-memory implementations

## Key Patterns

### Reactive Programming
```java
Flux.fromIterable(request.networks())
    .flatMap(network -> blockchainConsumerService.streamTransactions(network))
    .filter(tx -> bloomFilterService.mightContain(tx.network(), tx.hash()))
    .transform(repository::saveAll)
```

### Data Models
```java
public record Transaction(String hash, String from, String to,
                         String network, Instant timestamp, boolean confirmed) {}
```

### Bloom Filter Deduplication
```java
public synchronized boolean mightContain(String network, String address) {
    String key = network + address;
    for (int seed : SEEDS) {
        if (!bitSet.get(indexFor(key, seed))) return false;
    }
    return true;
}
```

### WebSocket Connection Management
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

### Provider Failover with Resilience4j
```java
@Retry(name = "web3")
@CircuitBreaker(name = "web3")
public Mono<BigInteger> latestBlockNumber(String httpUrl) {
    return Mono.fromFuture(httpClient(httpUrl).ethBlockNumber().sendAsync())
               .map(EthBlockNumber::getBlockNumber);
}
```

### Reactive Stream Composition
```java
// Chain operations with error handling
Flux.fromIterable(request.networks())
    .flatMap(network -> blockchainConsumerService.streamTransactions(network))
    .filter(tx -> tx.from().equalsIgnoreCase(request.value()))
    .filter(tx -> bloomFilterService.mightContain(tx.network(), tx.hash()))
    .doOnNext(tx -> bloomFilterService.add(tx.network(), tx.hash()))
    .transform(repository::saveAll)
    .doOnError(error -> log.error("Error processing transaction", error))
    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
```

### Data Models with Java Records
```java
public record Transaction(String hash, String from, String to,
                         String network, Instant timestamp, boolean confirmed) {}

public record TrackingRequest(String type, String value, List<String> networks) {}

public record ContractEvent(String transactionHash, String contractAddress,
                           String eventSignature, List<String> topics,
                           String data, Instant timestamp) {}
```

### Repository Pattern with Reactive API
```java
@Repository
public class InMemoryTransactionRepository implements TransactionRepository {
    private final List<Transaction> storage = new CopyOnWriteArrayList<>();

    @Override
    public Flux<Transaction> saveAll(Flux<Transaction> transactions) {
        return transactions.doOnNext(storage::add);
    }
}
```

### Custom Bloom Filter with BitSet
```java
public synchronized boolean mightContain(String network, String address) {
    String key = network + address;
    for (int seed : SEEDS) {
        if (!bitSet.get(indexFor(key, seed))) return false;
    }
    return true;
}

public synchronized void add(String network, String address) {
    String key = network + address;
    for (int seed : SEEDS) {
        bitSet.set(indexFor(key, seed));
    }
}
```

### Web3j Client Management
```java
private final ConcurrentMap<String, Web3j> httpClients = new ConcurrentHashMap<>();
private final ConcurrentMap<String, Web3j> wsClients = new ConcurrentHashMap<>();

private Web3j httpClient(String url) {
    return httpClients.computeIfAbsent(url, u -> Web3j.build(new HttpService(u)));
}
```

### Reactive Error Handling
```java
public Flux<Transaction> track(TrackingRequest request) {
    return Flux.fromIterable(request.networks())
        .flatMap(network -> blockchainConsumerService.streamTransactions(network)
            .filter(tx -> tx.from().equalsIgnoreCase(request.value()))
            .filter(tx -> {
                if (bloomFilterService.mightContain(tx.network(), tx.hash())) {
                    return false;
                }
                bloomFilterService.add(tx.network(), tx.hash());
                return true;
            }))
        .transform(repository::saveAll)
        .onErrorResume(Web3jException.class, error -> {
            log.warn("Web3j error for network, continuing with other networks", error);
            return Flux.empty();
        });
}
```

### Configuration with @ConfigurationProperties
```java
@Configuration
@ConfigurationProperties(prefix = "networks")
public class BlockchainNetworkConfig {
    private Map<String, List<String>> endpoints;

    public Map<String, List<String>> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Map<String, List<String>> endpoints) {
        this.endpoints = endpoints;
    }
}
```

### WebSocket Handler Implementation
```java
@Component
public class TrackingWebSocketHandler implements WebSocketHandler {
    private final TransactionTrackingService trackingService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.receive()
            .map(msg -> msg.getPayloadAsText())
            .flatMap(text -> {
                try {
                    return Mono.just(mapper.readValue(text, TrackingRequest.class));
                } catch (Exception e) {
                    return Mono.empty();
                }
            })
            .flatMap(trackingService::track)
            .map(tx -> session.textMessage(tx.hash()))
            .as(session::send);
    }
}
```

### REST Controller with Reactive Return Types
```java
@RestController
@RequestMapping("/tracking")
public class TrackingController {
    private final TransactionTrackingService trackingService;

    @PostMapping("/transactions")
    public Flux<Transaction> track(@RequestBody TrackingRequest request) {
        return trackingService.track(request);
    }
}
```

### Testing with StepVerifier
```java
@Test
void getLatestBlock() {
    StepVerifier.create(service.latestBlockNumber(url))
        .assertNext(block -> assertTrue(block.signum() > 0))
        .verifyTimeout(Duration.ofSeconds(30));
}
```

### Integration Testing with Parameterized Tests
```java
@ParameterizedTest(name = "{0} latest block")
@MethodSource("httpProviders")
void getLatestBlock(String name, String url) {
    assumeTrue(url != null && !url.isBlank());
    StepVerifier.create(service.latestBlockNumber(url))
        .assertNext(block -> assertTrue(block.signum() > 0))
        .verifyTimeout(Duration.ofSeconds(30));
}
```

## Development Workflow
```bash
./mvnw spring-boot:run          # Development with hot reload
./mvnw clean package           # Build JAR
docker-compose up --build      # Docker development
```

## Environment Setup
```bash
export ETHEREUM_WS_URL=wss://mainnet.infura.io/ws/v3/YOUR_KEY
export POLYGON_WS_URL=wss://polygon-mainnet.infura.io/ws/v3/YOUR_KEY
```

## Key Files
- `BlockchainMonitorService.java` - Web3j client management
- `TransactionTrackingService.java` - Main business logic
- `BloomFilterService.java` - Custom deduplication
- `TrackingController.java` - REST API (`POST /tracking/transactions`)
- `TrackingWebSocketHandler.java` - Real-time WebSocket streaming

## Common Tasks

### Adding New Network
1. Add endpoint to `application.yaml` under `networks.endpoints`
2. Update `BlockchainNetworkConfig` if needed

### New Filter Criteria
1. Extend `BloomFilterService` key generation
2. Add logic to `TransactionTrackingService.track()`

### WebSocket Endpoint
1. Create `WebSocketHandler` implementation
2. Add `@Bean` in `WebSocketConfig`</content>
<parameter name="filePath">/Users/italo/Projects/bloco/blockchain-transaction-tracker/.github/copilot-instructions.md