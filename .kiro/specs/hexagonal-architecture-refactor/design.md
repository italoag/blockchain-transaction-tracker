# Design Document

## Overview

This design document outlines the refactoring of the blockchain transaction tracking microservice from a traditional layered architecture to a Hexagonal Architecture (Ports and Adapters) with Domain-Driven Design (DDD) principles and Rich Domain Models. The refactoring will maintain all existing functionality while improving code organization, testability, and business logic encapsulation.

The hexagonal architecture will isolate the business logic in the domain core, with all external concerns handled through ports (interfaces) and adapters (implementations). This approach will make the system more maintainable, testable, and adaptable to changing requirements.

## Architecture

### Hexagonal Architecture Structure

```
┌─────────────────────────────────────────────────────────────┐
│                    Infrastructure Layer                     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │
│  │   Web Adapters  │  │ Persistence     │  │ External    │  │
│  │   (REST/WS)     │  │ Adapters        │  │ Service     │  │
│  │                 │  │                 │  │ Adapters    │  │
│  └─────────────────┘  └─────────────────┘  └─────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                   Application Layer                         │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │
│  │  Application    │  │   Command       │  │   Query     │  │
│  │   Services      │  │   Handlers      │  │  Handlers   │  │
│  └─────────────────┘  └─────────────────┘  └─────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                            │
│  ┌─────────────────┐  ┌────────────────┐  ┌──────────────┐  │
│  │   Aggregates    │  │  Domain        │  │   Ports      │  │
│  │   & Entities    │  │  Services      │  │ (Interfaces) │  │
│  │                 │  │                │  │              │  │
│  └─────────────────┘  └────────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Bounded Contexts

Based on the current functionality, we identify the following bounded contexts:

1. **Transaction Tracking Context**: Core business logic for tracking blockchain transactions
2. **Bridge Detection Context**: Specialized logic for detecting cross-chain bridge events  
3. **Blockchain Integration Context**: External blockchain network connectivity
4. **Notification Context**: Real-time streaming and notifications

## Components and Interfaces

### Domain Layer

#### Aggregates and Entities

**Transaction Aggregate Root**
```java
public class Transaction {
    private final TransactionId id;
    private final Address from;
    private final Address to;
    private final Network network;
    private final Timestamp timestamp;
    private TransactionStatus status;
    
    // Rich domain methods
    public boolean isConfirmed();
    public boolean involvesAddress(Address address);
    public void confirm();
    public List<DomainEvent> getUncommittedEvents();
}
```

**BridgeEvent Aggregate Root**
```java
public class BridgeEvent {
    private final BridgeEventId id;
    private final Network sourceNetwork;
    private final Network targetNetwork;
    private final BridgeContract bridgeContract;
    private final TransactionHash transactionHash;
    private final Timestamp timestamp;
    
    // Rich domain methods
    public boolean isCrossChain();
    public boolean isValidBridge();
    public static BridgeEvent fromTransaction(Transaction transaction);
}
```

**Value Objects**
```java
public record TransactionId(String value) { }
public record Address(String value) { 
    // Validation logic
    public boolean isValid();
}
public record Network(String name) { }
public record BridgeContract(Address address) { }
```

#### Domain Services

**TransactionTrackingDomainService**
```java
public interface TransactionTrackingDomainService {
    boolean shouldTrackTransaction(Transaction transaction, TrackingCriteria criteria);
    boolean isDuplicateTransaction(Transaction transaction);
}
```

**BridgeDetectionDomainService**
```java
public interface BridgeDetectionDomainService {
    Optional<BridgeEvent> detectBridgeEvent(Transaction transaction);
    boolean isBridgeTransaction(Transaction transaction);
}
```

#### Ports (Outbound Interfaces)

**TransactionRepository**
```java
public interface TransactionRepository {
    Mono<Transaction> save(Transaction transaction);
    Flux<Transaction> saveAll(Flux<Transaction> transactions);
    Flux<Transaction> findByAddress(Address address);
}
```

**BridgeEventRepository**
```java
public interface BridgeEventRepository {
    Mono<BridgeEvent> save(BridgeEvent bridgeEvent);
    Flux<BridgeEvent> findByNetwork(Network network);
}
```

**BlockchainGateway**
```java
public interface BlockchainGateway {
    Flux<Transaction> streamTransactions(Network network);
    Mono<Block> getBlock(Network network, String blockHash);
}
```

**DuplicationCheckService**
```java
public interface DuplicationCheckService {
    boolean mightContain(Network network, TransactionHash hash);
    void add(Network network, TransactionHash hash);
}
```

**NotificationPublisher**
```java
public interface NotificationPublisher {
    void publishTransaction(Transaction transaction);
    void publishBridgeEvent(BridgeEvent bridgeEvent);
}
```

### Application Layer

#### Application Services

**TransactionTrackingApplicationService**
```java
@Service
public class TransactionTrackingApplicationService {
    public Flux<Transaction> trackTransactions(TrackTransactionsCommand command);
    public Flux<Transaction> getTransactionHistory(GetTransactionHistoryQuery query);
}
```

**BridgeEventApplicationService**
```java
@Service
public class BridgeEventApplicationService {
    public Flux<BridgeEvent> trackBridgeEvents(TrackBridgeEventsCommand command);
    public Flux<BridgeEvent> getBridgeEventHistory(GetBridgeEventHistoryQuery query);
}
```

#### Commands and Queries

**Commands**
```java
public record TrackTransactionsCommand(
    List<Address> addresses,
    List<Network> networks,
    TrackingOptions options
) { }

public record TrackBridgeEventsCommand(
    List<Network> networks,
    BridgeTrackingOptions options
) { }
```

**Queries**
```java
public record GetTransactionHistoryQuery(
    Address address,
    Optional<Network> network,
    Optional<DateRange> dateRange
) { }

public record GetBridgeEventHistoryQuery(
    Optional<Network> sourceNetwork,
    Optional<Network> targetNetwork,
    Optional<DateRange> dateRange
) { }
```

### Infrastructure Layer

#### Inbound Adapters (Primary)

**REST Controller Adapter**
```java
@RestController
@RequestMapping("/api/v1/tracking")
public class TransactionTrackingController {
    // Maps HTTP requests to application service calls
}
```

**WebSocket Handler Adapter**
```java
@Component
public class TransactionStreamingWebSocketHandler {
    // Handles WebSocket connections for real-time streaming
}
```

#### Outbound Adapters (Secondary)

**JPA Repository Adapter**
```java
@Repository
public class JpaTransactionRepository implements TransactionRepository {
    // Implements domain repository using JPA
}
```

**Web3 Blockchain Gateway Adapter**
```java
@Component
public class Web3BlockchainGateway implements BlockchainGateway {
    // Implements blockchain connectivity using Web3j
}
```

**Guava Bloom Filter Adapter**
```java
@Component
public class GuavaBloomFilterDuplicationCheckService implements DuplicationCheckService {
    // Implements duplication checking using Guava Bloom Filter
}
```

**WebSocket Notification Publisher Adapter**
```java
@Component
public class WebSocketNotificationPublisher implements NotificationPublisher {
    // Publishes notifications via WebSocket
}
```

## Data Models

### Domain Models

The domain models will be rich objects with behavior, not just data containers:

```java
public class Transaction {
    // Value objects for type safety
    private final TransactionId id;
    private final Address from;
    private final Address to;
    private final Network network;
    private final Amount amount;
    private final Timestamp timestamp;
    private TransactionStatus status;
    private final List<DomainEvent> uncommittedEvents;
    
    // Business logic methods
    public boolean isConfirmed() {
        return status == TransactionStatus.CONFIRMED;
    }
    
    public boolean involvesAddress(Address address) {
        return from.equals(address) || to.equals(address);
    }
    
    public void confirm() {
        if (status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Only pending transactions can be confirmed");
        }
        this.status = TransactionStatus.CONFIRMED;
        this.addDomainEvent(new TransactionConfirmedEvent(this.id));
    }
    
    public boolean isBridgeTransaction() {
        return from.isBridgeContract() || to.isBridgeContract();
    }
}
```

### Persistence Models

Infrastructure layer will have separate persistence models that map to/from domain models:

```java
@Entity
@Table(name = "transactions")
public class TransactionEntity {
    @Id
    private String id;
    private String fromAddress;
    private String toAddress;
    private String network;
    private BigDecimal amount;
    private Instant timestamp;
    private String status;
    
    // Mapping methods
    public static TransactionEntity fromDomain(Transaction transaction);
    public Transaction toDomain();
}
```

## Error Handling

### Domain Exceptions

```java
public class TransactionTrackingException extends DomainException {
    public TransactionTrackingException(String message, Throwable cause);
}

public class InvalidAddressException extends DomainException {
    public InvalidAddressException(String address);
}

public class NetworkNotSupportedException extends DomainException {
    public NetworkNotSupportedException(String network);
}
```

### Application Layer Error Handling

```java
@Component
public class GlobalExceptionHandler {
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex);
    
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex);
}
```

### Infrastructure Error Handling

```java
@Component
public class BlockchainConnectionErrorHandler {
    public Mono<Transaction> handleConnectionError(Throwable error);
    public Flux<Transaction> retryWithBackoff(Flux<Transaction> source);
}
```

## Testing Strategy

### Domain Layer Testing

**Unit Tests for Aggregates**
```java
class TransactionTest {
    @Test
    void shouldConfirmPendingTransaction() {
        // Test domain logic without external dependencies
    }
    
    @Test
    void shouldThrowExceptionWhenConfirmingNonPendingTransaction() {
        // Test business rule enforcement
    }
}
```

**Unit Tests for Domain Services**
```java
class BridgeDetectionDomainServiceTest {
    @Test
    void shouldDetectBridgeTransaction() {
        // Test domain service logic
    }
}
```

### Application Layer Testing

**Integration Tests for Application Services**
```java
@SpringBootTest
class TransactionTrackingApplicationServiceTest {
    @MockBean
    private TransactionRepository transactionRepository;
    
    @MockBean
    private BlockchainGateway blockchainGateway;
    
    @Test
    void shouldTrackTransactionsForAddress() {
        // Test orchestration logic with mocked dependencies
    }
}
```

### Infrastructure Layer Testing

**Adapter Tests**
```java
@DataJpaTest
class JpaTransactionRepositoryTest {
    @Test
    void shouldSaveAndRetrieveTransaction() {
        // Test persistence adapter
    }
}

@WebFluxTest
class TransactionTrackingControllerTest {
    @Test
    void shouldReturnTransactionsForValidRequest() {
        // Test web adapter
    }
}
```

### End-to-End Testing

**Full System Tests**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionTrackingIntegrationTest {
    @Test
    void shouldTrackTransactionsEndToEnd() {
        // Test complete workflow from HTTP request to database persistence
    }
}
```

## Migration Strategy

### Phase 1: Domain Layer Creation
- Create new domain models with rich behavior
- Implement domain services
- Define ports (interfaces)
- Create domain exceptions

### Phase 2: Application Layer Refactoring
- Create application services
- Implement command/query handlers
- Add application-level error handling

### Phase 3: Infrastructure Adaptation
- Implement adapters for existing repositories
- Create new blockchain gateway adapters
- Refactor controllers to use application services
- Update configuration and dependency injection

### Phase 4: Testing and Validation
- Add comprehensive test coverage
- Validate all existing functionality works
- Performance testing to ensure no degradation
- Update documentation

### Phase 5: Cleanup
- Remove old layered architecture code
- Update package structure
- Final documentation updates