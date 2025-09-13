# Improvement Tasks Checklist

Below is a logically ordered, actionable checklist covering both architectural and code-level improvements. Each item is framed to be implementable and verifiable.

[ ] 1. Align configuration binding for networks.endpoints with list syntax expected by BlockchainNetworkConfig
   - Convert application.yaml to use lists per network (ethereum:, polygon:, etc.).
   - Document environment variable list binding (NETWORKS_ENDPOINTS_<NETWORK>_0, _1, ...).
   - Acceptance: an integration-style configuration properties test confirms Map<String, List<String>> is populated.
   
[ ] 2. Fix Dockerfile jar path mismatch with pom version (0.1.0)
   - Replace COPY target/wallet-0.0.1-SNAPSHOT.jar app.jar with COPY target/wallet-*.jar app.jar (or 0.1.0 explicitly).
   - Acceptance: docker compose up --build starts successfully after mvn package.

[ ] 3. Update README to reflect accurate stack and usage
   - Correct Web3j version (4.14.0), add build commands, test recipes, and provider URL env variables (HTTP and WS).
   - Include configuration examples for list-based endpoints and env var patterns.

[ ] 4. Introduce structured logging (SLF4J) and replace System.err prints
   - Add private static final Logger to services; replace prints with log.info/warn/error (with context).
   - Acceptance: logs appear with consistent format and levels; no System.err usage remains.

[ ] 5. Add @PreDestroy cleanup to BlockchainConsumerService
   - Close all cached Web3j clients and underlying transports on shutdown; clear maps.
   - Acceptance: application terminates without lingering sockets; no resource leak warnings.

[ ] 6. Centralize Web3j client creation in a Web3ClientFactory
   - Extract duplicated HTTP/WS client construction from BlockchainMonitorService and BlockchainConsumerService.
   - Handle WebSocket connect() exceptions and configurable connection timeouts.
   - Acceptance: both services use the factory; unit tests cover HTTP and WS creation and failure cases.

[ ] 7. Enhance endpoint selection and failover in BlockchainConsumerService
   - On stream error, rotate to next configured endpoint and rebuild client; apply exponential backoff with max cap.
   - Acceptance: with first endpoint failing, service auto-switches to the next and resumes streaming.
   
[ ] 8. Apply Resilience4j (@Retry/@CircuitBreaker) to BlockchainConsumerService streaming operations
   - Reuse existing "web3" instance from application.yaml; ensure sensible wait durations and thresholds.
   - Acceptance: transient failures are retried; circuit opens on repeated failures.

[ ] 9. Strengthen error handling in BlockchainMonitorService.latestBlockNumber
   - Check hasError() on EthBlockNumber response and surface meaningful exceptions.
   - Acceptance: invalid endpoint test emits an error with actionable message.
   
[ ] 10. Make timeouts and intervals configurable
    - Externalize monitorLogs polling interval, RPC timeouts, and retry backoff via application.yaml (e.g., web3.timeouts, web3.poll-interval).
    - Acceptance: overriding properties changes behavior without code changes.

[ ] 11. Expose a WebSocket endpoint mapping
    - Add SimpleUrlHandlerMapping to map /ws/track (or similar) to TrackingWebSocketHandler with defined order.
    - Acceptance: ws://.../ws/track accepts connections and streams messages.

[ ] 12. Inject and reuse ObjectMapper via Spring configuration
    - Provide an ObjectMapper @Bean; inject into TrackingWebSocketHandler.
    - Send a JSON error message (or close with policy) on parse failures instead of silently dropping.

[ ] 13. Tighten CORS configuration
    - Replace allowedOrigins("*") with a property-driven list of allowed origins.
    - Acceptance: CORS honors configured origins; defaults are safe for dev.

[ ] 14. Add Micrometer metrics for observability
    - Counters/gauges: blocks received, tx processed, bloom hits/misses, connection status per endpoint.
    - Acceptance: metrics visible under /actuator/metrics and tagged by network/endpoint.

[ ] 15. Improve BloomFilterService ergonomics and performance
    - Add clear/reset operation; allow capacity/size configuration; document false-positive rate trade-offs.
    - Acceptance: unit tests cover add, mightContain, reset behavior; configurable size is honored.

[ ] 16. Add concurrency tests for BloomFilterService
    - Simulate parallel add/check from multiple threads; ensure thread-safety and correctness.

[ ] 17. Expand TransactionTrackingService tests
    - Cover deduplication across multiple networks; ensure repository receives only first-seen tx hashes.

[ ] 18. Enhance BridgeDetectionService heuristics
    - Replace "contains('bridge')" with configurable patterns or known contract lists/signatures; add tests.

[ ] 19. Unify package/service layering
    - Rationalize dev.bloco.blockchain vs dev.bloco.wallet packages; consider moving BlockchainMonitorService to wallet.service.web3 or a dedicated web3 module.
    - Acceptance: clear layering (web3 access, domain services, handlers/controllers).

[ ] 20. Validate configuration properties
    - Annotate BlockchainNetworkConfig with @Validated and constraints (@NotEmpty) to fail fast on invalid config.

[ ] 21. Standardize Reactor threading policies
    - Ensure all blocking work (client creation, connect) runs on boundedElastic when called from reactive flows; avoid blocking event loops.
    - Acceptance: no Reactor blocking warnings; predictable schedulers usage.

[ ] 22. Backpressure and buffer safety
    - Apply onBackpressureBuffer with bounded size and drop/Latest semantics where high-throughput streams are possible; make limits configurable.

[ ] 23. Clean up lastBlocks memory in BlockchainMonitorService
    - Add TTL or cleanup for lastBlocks entries keyed by httpUrl; handle removal when endpoints are no longer used.

[ ] 24. Add graceful shutdown hooks for long-lived streams
    - Ensure Fluxes created with publish().refCount(...) terminate cleanly on shutdown; add disposal checks.

[ ] 25. Add structured error types
    - Introduce domain exceptions for endpoint failures, RPC errors, and parsing errors; improve messages and logs.

[ ] 26. Expand integration tests with env guards
    - Add tests for HTTP logs retrieval and WebSocket subscriptions per provider with Assume guards (pattern from Web3ProvidersIntegrationTest).
    - Include negative tests (invalid topic/address formatting) to validate validation logic.

[ ] 27. Introduce configuration for known topics/addresses lists
    - Externalize common ERC-20 Transfer topic and known token addresses to properties for reuse in tests and runtime.

[ ] 28. Harden input validation
    - Validate hex formatting for addresses and topics when adding/removing; reject invalid inputs.

[ ] 29. Add build tooling improvements
    - Configure Maven Surefire/Failsafe plugins explicitly; add Spotless/Checkstyle for formatting and static checks.

[ ] 30. Container hardening and size optimization
    - Optionally switch to distroless base image; set non-root user; add minimal JVM flags for containers.

[ ] 31. Observability: add log correlation and context
    - Include network and endpoint tags in logs; optionally Mapped Diagnostic 
    
[ ] 32. Document operational runbooks
    - Add docs for rotating provider endpoints, tuning retries/backoffs, and diagnosing provider throttling.

[ ] 33. Provide examples and tooling
    - Add sample curl and WebSocket examples; optional Postman collection for /tracking endpoints.

[ ] 34. Align versions across docs and code
    - Ensure README and docs mention the same Spring Boot, Java, Web3j, and library versions as pom.xml.
