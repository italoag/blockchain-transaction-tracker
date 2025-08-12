package dev.bloco.blockchain;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthLog;

import reactor.test.StepVerifier;

/**
 * Integration tests that validate connectivity with different blockchain
 * providers over HTTP polling and WebSocket pub/sub endpoints. Tests are
 * skipped when the respective provider URL is not configured.
 */
public class Web3ProvidersIntegrationTest {

    private static final String TEST_ADDRESS = "0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045";
    private static final String USDT_CONTRACT = "0xdAC17F958D2ee523a2206206994597C13D831ec7";
    private static final String TRANSFER_TOPIC =
            "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a6b7fa1c012";

    private final BlockchainMonitorService service = new BlockchainMonitorService();

    static Stream<Arguments> httpProviders() {
        List<Arguments> providers = Stream.of(
                provider("Infura", System.getenv("INFURA_HTTP_URL")),
                provider("Alchemy", System.getenv("ALCHEMY_HTTP_URL")),
                provider("AllNodes", System.getenv("ALLNODES_HTTP_URL")),
                provider("Tenderly", System.getenv("TENDERLY_HTTP_URL")))
                .filter(p -> p.url != null && !p.url.isBlank())
                .map(p -> Arguments.of(p.name, p.url))
                .toList();
        return providers.isEmpty() ? Stream.of(Arguments.of("none", "")) : providers.stream();
    }

    static Stream<Arguments> wsProviders() {
        List<Arguments> providers = Stream.of(
                provider("Infura", System.getenv("INFURA_WS_URL")),
                provider("Alchemy", System.getenv("ALCHEMY_WS_URL")),
                provider("AllNodes", System.getenv("ALLNODES_WS_URL")),
                provider("Tenderly", System.getenv("TENDERLY_WS_URL")))
                .filter(p -> p.url != null && !p.url.isBlank())
                .map(p -> Arguments.of(p.name, p.url))
                .toList();
        return providers.isEmpty() ? Stream.of(Arguments.of("none", "")) : providers.stream();
    }

    private static Provider provider(String name, String url) {
        return new Provider(name, url);
    }

    private record Provider(String name, String url) {}

    @ParameterizedTest(name = "{0} latest block")
    @MethodSource("httpProviders")
    void getLatestBlock(String name, String url) {
        assumeTrue(url != null && !url.isBlank());
        StepVerifier.create(service.latestBlockNumber(url))
                .assertNext(block -> assertTrue(block.signum() > 0, name + " returned invalid block number"))
                .verifyTimeout(Duration.ofSeconds(30));
    }

    @ParameterizedTest(name = "{0} transaction count")
    @MethodSource("httpProviders")
    void getTransactionCount(String name, String url) {
        assumeTrue(url != null && !url.isBlank());
        StepVerifier.create(service.transactionCount(url, TEST_ADDRESS))
                .assertNext(count -> assertNotNull(count, name + " returned null transaction count"))
                .verifyTimeout(Duration.ofSeconds(30));
    }

    @ParameterizedTest(name = "{0} recent logs")
    @MethodSource("httpProviders")
    void getRecentLogs(String name, String url) {
        assumeTrue(url != null && !url.isBlank());
        service.addAddress(USDT_CONTRACT);
        service.addEventTopic(TRANSFER_TOPIC);
        StepVerifier.create(service.monitorLogs(url).take(1))
                .assertNext(log -> assertNotNull(log, name + " returned null logs"))
                .verifyTimeout(Duration.ofSeconds(60));
    }

    @ParameterizedTest(name = "{0} websocket blocks")
    @MethodSource("wsProviders")
    void subscribeBlocks(String name, String url) {
        assumeTrue(url != null && !url.isBlank());
        StepVerifier.create(service.newBlocks(url).take(1))
                .assertNext(block -> assertNotNull(block, name + " did not receive block"))
                .verifyTimeout(Duration.ofSeconds(60));
    }

    @Test
    void invalidEndpointEmitsError() {
        StepVerifier.create(service.latestBlockNumber("http://127.0.0.1:1"))
                .expectError()
                .verify();
    }

    @Test
    void addAndRemoveAddresses() {
        service.addAddress("0x1");
        service.addAddress("0x2");
        assertTrue(service.isMonitoredAddress("0x1"));
        service.removeAddress("0x1");
        assertFalse(service.isMonitoredAddress("0x1"));
        assertTrue(service.isMonitoredAddress("0x2"));
    }

    @Test
    void addAndRemoveTopics() {
        service.addEventTopic("0xabc");
        service.addEventTopic("0xdef");
        assertTrue(service.isMonitoredTopic("0xabc"));
        service.removeEventTopic("0xabc");
        assertFalse(service.isMonitoredTopic("0xabc"));
        assertTrue(service.isMonitoredTopic("0xdef"));
    }
}
