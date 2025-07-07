package dev.bloco.wallet.service;

import dev.bloco.wallet.config.BlockchainNetworkConfig;
import dev.bloco.wallet.model.Block;
import dev.bloco.wallet.model.Transaction;
import dev.bloco.wallet.util.NetworkUtils;
import io.reactivex.Flowable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class BlockchainConsumerService {

    private final BlockchainNetworkConfig networkConfig;
    private final Map<String, Web3j> clients = new ConcurrentHashMap<>();

    public BlockchainConsumerService(BlockchainNetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    private Web3j buildClient(String endpoint) {
        try {
            if (endpoint.startsWith("ws")) {
                WebSocketClient client = new WebSocketClient(new URI(endpoint));
                WebSocketService service = new WebSocketService(client, false);
                service.connect();
                return Web3j.build(service);
            }
            return Web3j.build(new HttpService(endpoint));
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to " + endpoint, e);
        }
    }

    private Web3j getWeb3j(String network) {
        return clients.computeIfAbsent(network, n -> {
            Map<String, List<String>> endpointsMap = networkConfig.getEndpoints();
            List<String> endpoints = endpointsMap.get(n);
            if (endpoints == null || endpoints.isEmpty()) {
                throw new IllegalArgumentException("No endpoints configured for network: " + n);
            }
            for (String endpoint : endpoints) {
                try {
                    return buildClient(endpoint);
                } catch (RuntimeException e) {
                    // Log the failure and try the next endpoint
                    System.err.println("Failed to connect to endpoint: " + endpoint + ". Trying next...");
                }
            }
            throw new RuntimeException("Failed to connect to any endpoint for network: " + n);
        });
    }

    private Block toBlock(String network, EthBlock.Block block) {
        Instant timestamp = Instant.ofEpochSecond(block.getTimestamp().longValue());
        List<Transaction> transactions = block.getTransactions().stream()
                .map(txResult -> (EthBlock.TransactionObject) txResult.get())
                .map(tx -> new Transaction(
                        tx.getHash(),
                        tx.getFrom(),
                        tx.getTo(),
                        network,
                        timestamp,
                        tx.getBlockHash() != null
                ))
                .collect(Collectors.toList());
        return new Block(block.getNumber().longValue(), timestamp, transactions);
    }

    public Flux<Block> streamBlocks(String network) {
        return Flux.defer(() -> {
                    Web3j web3j = getWeb3j(network);
                    Flowable<EthBlock> flowable = web3j.blockFlowable(true);
                    return Flux.from(flowable)
                            .map(ethBlock -> toBlock(network, ethBlock.getBlock()));
                })
                .retryWhen(Retry.backoff(10, NetworkUtils.reconnectBackoff(1)).maxBackoff(Duration.ofMinutes(5)));
    }

    public Flux<Transaction> streamTransactions(String network) {
        return streamBlocks(network)
                .flatMap(block -> Flux.fromIterable(block.transactions()));
    }
}
