package dev.bloco.blockchain;

import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.reactivex.Flowable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketService;

/**
 * Service capable of monitoring multiple blockchain operations concurrently. It
 * allows registering and removing addresses and event topics on the fly without
 * impacting ongoing monitoring.
 */
@Service
public class BlockchainMonitorService {

    private final ConcurrentMap<String, Web3j> httpClients = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Web3j> wsClients = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, BigInteger> lastBlocks = new ConcurrentHashMap<>();

    private final Set<String> addresses = ConcurrentHashMap.newKeySet();
    private final Set<String> topics = ConcurrentHashMap.newKeySet();
    private volatile BloomFilter<String> addressBloom = BloomFilter.create(
            Funnels.unencodedCharsFunnel(), 100);
    private volatile BloomFilter<String> topicBloom = BloomFilter.create(
            Funnels.unencodedCharsFunnel(), 100);

    private Web3j httpClient(String url) {
        return httpClients.computeIfAbsent(url, u -> Web3j.build(new HttpService(u)));
    }

    private Web3j wsClient(String url) {
        return wsClients.computeIfAbsent(url, u -> {
            WebSocketService service = new WebSocketService(u, true);
            try {
                service.connect();
            } catch (Exception e) {
                throw new RuntimeException("Failed to connect to websocket", e);
            }
            return Web3j.build(service);
        });
    }

    public void addAddress(String address) {
        addresses.add(address);
        rebuildAddressBloom();
    }

    public void removeAddress(String address) {
        addresses.remove(address);
        rebuildAddressBloom();
    }

    public boolean isMonitoredAddress(String address) {
        return addresses.contains(address);
    }

    public void addEventTopic(String topic) {
        topics.add(topic);
        rebuildTopicBloom();
    }

    public void removeEventTopic(String topic) {
        topics.remove(topic);
        rebuildTopicBloom();
    }

    public boolean isMonitoredTopic(String topic) {
        return topics.contains(topic);
    }

    private void rebuildAddressBloom() {
        BloomFilter<String> filter = BloomFilter.create(Funnels.unencodedCharsFunnel(),
                Math.max(addresses.size(), 1));
        addresses.forEach(filter::put);
        addressBloom = filter;
    }

    private void rebuildTopicBloom() {
        BloomFilter<String> filter = BloomFilter.create(Funnels.unencodedCharsFunnel(),
                Math.max(topics.size(), 1));
        topics.forEach(filter::put);
        topicBloom = filter;
    }

    @Retry(name = "web3")
    @CircuitBreaker(name = "web3")
    public Mono<BigInteger> latestBlockNumber(String httpUrl) {
        return Mono.fromFuture(httpClient(httpUrl).ethBlockNumber().sendAsync())
                .map(EthBlockNumber::getBlockNumber);
    }

    @Retry(name = "web3")
    @CircuitBreaker(name = "web3")
    public Mono<BigInteger> transactionCount(String httpUrl, String address) {
        return Mono.fromFuture(httpClient(httpUrl)
                .ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).sendAsync())
                .map(EthGetTransactionCount::getTransactionCount);
    }

    @Retry(name = "web3")
    @CircuitBreaker(name = "web3")
    public Flux<EthBlock> newBlocks(String wsUrl) {
        return Flux.defer(() -> {
            Flowable<EthBlock> flowable = wsClient(wsUrl).blockFlowable(true);
            return Flux.from(flowable);
        });
    }

    @Retry(name = "web3")
    @CircuitBreaker(name = "web3")
    public Flux<EthLog.LogResult> monitorLogs(String httpUrl) {
        return Flux.interval(Duration.ofSeconds(5))
                .flatMap(tick -> fetchLogs(httpUrl))
                .flatMapIterable(list -> list)
                .filter(this::matchesFilter)
                .publish()
                .refCount();
    }

    private Mono<List<EthLog.LogResult>> fetchLogs(String httpUrl) {
        Web3j client = httpClient(httpUrl);
        return Mono.fromFuture(client.ethBlockNumber().sendAsync()).flatMap(bn -> {
            BigInteger latest = bn.getBlockNumber();
            BigInteger start = lastBlocks.getOrDefault(httpUrl, latest);
            lastBlocks.put(httpUrl, latest);
            EthFilter filter = new EthFilter(new DefaultBlockParameterNumber(start),
                    new DefaultBlockParameterNumber(latest), List.copyOf(addresses));
            topics.forEach(filter::addOptionalTopics);
            return Mono.fromFuture(client.ethGetLogs(filter).sendAsync()).map(EthLog::getLogs);
        });
    }

    private boolean matchesFilter(EthLog.LogResult logResult) {
        if (logResult instanceof EthLog.LogObject log) {
            boolean addressMatch = addressBloom.mightContain(log.getAddress());
            boolean topicMatch = log.getTopics().stream().anyMatch(topicBloom::mightContain);
            return addressMatch && topicMatch;
        }
        return false;
    }
}
