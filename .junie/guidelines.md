# Development Guidelines (Project-specific)

This document captures high‑signal, project‑specific notes to speed up development, testing, and debugging of this repository.

## Build and Configuration

- Toolchain
  - Java 21 (Temurin recommended). Project uses Spring Boot 3.5.3 and Maven Wrapper.
  - Verify: `./mvnw -v` should report Java 21.
- Build
  - Fast build without tests: `./mvnw -q -DskipTests package`
  - Full build + tests: `./mvnw -q verify`
  - Run app (dev): `./mvnw spring-boot:run`
- Runtime configuration (application.yaml)
  - networks.endpoints are read from env vars:
    - ETHEREUM_WS_URL → networks.endpoints.ethereum
    - POLYGON_WS_URL → networks.endpoints.polygon
  - Resilience4j: a retry and circuit breaker named "web3" is configured. If you wrap new reactive IO calls, consider using the same instance names for consistent behavior.
- Docker
  - Compose: `docker-compose up --build`
  - Important: Dockerfile expects `target/wallet-0.0.1-SNAPSHOT.jar`, but pom.xml declares version `0.1.0`. Either:
    - update Dockerfile COPY line to the actual JAR produced by Spring Boot (e.g., `wallet-0.1.0.jar`), or
    - after packaging, copy/rename: `cp target/wallet-0.1.0.jar target/wallet-0.0.1-SNAPSHOT.jar` before building the image.
  - Base image is Eclipse Temurin 21 JRE.

## Testing

- Frameworks
  - JUnit 5 (Jupiter) + Spring Boot Starter Test + Reactor Test (`StepVerifier`) are available.
- Quick commands
  - All tests: `./mvnw -q test`
  - One class: `./mvnw -q -Dtest=dev.bloco.wallet.service.BloomFilterServiceTest test`
  - One method: `./mvnw -q -Dtest=dev.bloco.wallet.service.BloomFilterServiceTest#addAndCheck test`
- Integration-like tests for Web3 connectivity
  - Class: `dev.bloco.blockchain.Web3ProvidersIntegrationTest`
  - These tests auto-skip when provider URLs are not configured (they use `Assumptions.assumeTrue`). The negative-path test (`invalidEndpointEmitsError`) always runs.
  - To enable real network checks, set any of the following env vars (HTTP and WS supported):
    - INFURA_HTTP_URL, INFURA_WS_URL
    - ALCHEMY_HTTP_URL, ALCHEMY_WS_URL
    - ALLNODES_HTTP_URL, ALLNODES_WS_URL
    - TENDERLY_HTTP_URL, TENDERLY_WS_URL
  - Example (bash):
    - `export INFURA_HTTP_URL=https://mainnet.infura.io/v3/<project_id>`
    - `export INFURA_WS_URL=wss://mainnet.infura.io/ws/v3/<project_id>`
- Adding new tests
  - Place tests under `src/test/java` mirroring package structure.
  - Prefer constructor injection in test subjects; for Reactor flows, use `StepVerifier` to assert emissions and completion.
  - Example minimal test (works with current setup):

    ```
    package dev.bloco.wallet.service;

    import org.junit.jupiter.api.Test;
    import static org.junit.jupiter.api.Assertions.*;

    class ExampleSanityTest {
        @Test
        void sanity() {
            assertEquals(4, 2 + 2);
        }
    }
    ```

  - To run just this test: `./mvnw -q -Dtest=dev.bloco.wallet.service.ExampleSanityTest test`
  - For Reactor-based services, a useful pattern:

    ```
    StepVerifier.create(service.track(request))
        .expectNextMatches(tx -> tx.hash() != null)
        .verifyComplete();
    ```

- Notes about current tests
  - `TransactionTrackingServiceTest` demonstrates mocking of `BlockchainConsumerService` and verifying stored results in the in-memory repo.
  - `BridgeEventTrackingServiceTest` covers both event detection and data persistence with Reactor pipelines.
  - `Web3ProvidersIntegrationTest` uses short timeouts (30–60s). Avoid setting provider URLs if you want a fast test cycle (skips the network tests).

## Additional Development Notes

- Reactive/WebFlux
  - The REST endpoint `/tracking/transactions` accepts a JSON `TrackingRequest` and returns a `Flux<Transaction>`.
    - `TrackingRequest` is a record: `type`, `value` (e.g., address), and `networks` (list of network ids like `eth`).
  - `TrackingWebSocketHandler` exists and emits transaction hashes over WebSockets, but there is currently no explicit `HandlerMapping` registering a WS path. If you intend to expose a WS endpoint, add a `HandlerMapping` bean to map a URI (e.g., `/ws/tracking`) to the handler.
  - Keep flows non-blocking; avoid blocking web3 calls on Reactor threads. Where web3j is used (in `BlockchainMonitorService`), ensure any future changes preserve non-blocking reactive boundaries.
- Concurrency and data structures
  - `BlockchainMonitorService` caches HTTP and WS `Web3j` clients per URL and tracks last seen blocks per endpoint. It uses `ConcurrentHashMap` and `@PreDestroy` to close clients.
  - A Bloom filter (Guava) is maintained for addresses and topics to speed up membership checks. When mutating monitored sets (`addAddress`, `removeAddress`, `addEventTopic`, `removeEventTopic`), the Bloom filter is rebuilt. If you change the expected cardinality, revisit the Bloom filter capacity parameters.
- Resilience/Observability
  - Resilience4j annotations (`@Retry`, `@CircuitBreaker`) are available for use; metrics integration is enabled via `resilience4j-micrometer` and Spring Boot Actuator.
- Repositories
  - `InMemoryTransactionRepository` and `InMemoryBridgeEventRepository` are in-memory and suited for tests/dev only. For persistence, replace with a proper reactive repository; ensure backpressure and serialization (ObjectMapper) remain compatible.
- CORS
  - `WebFluxConfig` allows all origins (`/**` → `*`). Adjust before exposing externally.

## Verified Example Flow (local)

- Ran unit tests successfully:
  - `./mvnw -q -Dtest=dev.bloco.wallet.WalletApplicationTests test`
  - `./mvnw -q -Dtest=dev.bloco.wallet.service.BloomFilterServiceTest test`
  - `./mvnw -q -Dtest=dev.bloco.blockchain.Web3ProvidersIntegrationTest test` (network-dependent tests auto-skipped if env vars absent)
- Demonstrated adding a new test (`ExampleSanityTest`) and running it; the file was removed after verification to keep the repo clean, but the snippet above can be re-used.

