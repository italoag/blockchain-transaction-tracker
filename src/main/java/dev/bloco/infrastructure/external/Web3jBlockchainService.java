package dev.bloco.infrastructure.external;

import dev.bloco.domain.entity.Block;
import dev.bloco.domain.entity.ContractEvent;
import dev.bloco.domain.entity.Transaction;
import dev.bloco.domain.service.BlockchainService;
import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import dev.bloco.domain.valueobject.TransactionHash;
import dev.bloco.domain.valueobject.TransactionValue;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.websocket.WebSocketService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Web3j-based implementation of BlockchainService.
 * Provides reactive access to blockchain data using Web3j library.
 */
public class Web3jBlockchainService implements BlockchainService {

    private final ConcurrentMap<String, Web3j> httpClients = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Web3j> wsClients = new ConcurrentHashMap<>();

    @Override
    @Retry(name = "web3")
    @CircuitBreaker(name = "web3")
    public Mono<BigInteger> getLatestBlockNumber(Network network) {
        return Mono.fromFuture(() -> getHttpClient(network).ethBlockNumber().sendAsync())
            .map(EthBlockNumber::getBlockNumber)
            .onErrorResume(IOException.class, e -> Mono.empty());
    }

    @Override
    public Flux<Block> streamBlocks(Network network) {
        return Flux.create(sink -> {
            try {
                Web3j wsClient = getWsClient(network);
                wsClient.blockFlowable(false).subscribe(
                    ethBlock -> {
                        try {
                            org.web3j.protocol.core.methods.response.EthBlock.Block block = ethBlock.getBlock();
                            if (block != null) {
                                Block domainBlock = convertToDomainBlock(block, network);
                                sink.next(domainBlock);
                            }
                        } catch (Exception e) {
                            sink.error(e);
                        }
                    },
                    sink::error,
                    sink::complete
                );
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    @Override
    @Retry(name = "web3")
    @CircuitBreaker(name = "web3")
    public Flux<Transaction> getTransactionsForAddress(Address address, Network network) {
        return Mono.fromFuture(() -> getHttpClient(network)
                .ethGetTransactionCount(address.getValue(), DefaultBlockParameter.valueOf("latest")).sendAsync())
            .flatMapMany(count -> {
                BigInteger txCount = count.getTransactionCount();
                return Flux.range(0, txCount.intValue())
                    .flatMap(i -> getTransactionByIndex(address, BigInteger.valueOf(i), network));
            });
    }

    @Override
    @Retry(name = "web3")
    @CircuitBreaker(name = "web3")
    public Mono<dev.bloco.domain.entity.Transaction> getTransactionByHash(TransactionHash hash, Network network) {
        return Mono.fromFuture(() -> getHttpClient(network).ethGetTransactionByHash(hash.getValue()).sendAsync())
            .filter(ethTx -> ethTx.getTransaction().isPresent())
            .map(ethTx -> ethTx.getTransaction().get())
            .map(tx -> convertToDomainTransaction(tx, network))
            .onErrorResume(IOException.class, e -> Mono.empty());
    }

    @Override
    @Retry(name = "web3")
    @CircuitBreaker(name = "web3")
    public Mono<Boolean> isContractAddress(Address address, Network network) {
        return Mono.fromFuture(() -> getHttpClient(network)
                .ethGetCode(address.getValue(), DefaultBlockParameter.valueOf("latest")).sendAsync())
            .map(code -> !code.getCode().equals("0x"))
            .onErrorResume(IOException.class, e -> Mono.just(false));
    }

    @Override
    public Flux<ContractEvent> getContractEvents(Address contractAddress, Network network) {
        return Flux.create(sink -> {
            try {
                Web3j httpClient = getHttpClient(network);

                // Create event filter for the contract
                org.web3j.protocol.core.methods.request.EthFilter filter =
                    new org.web3j.protocol.core.methods.request.EthFilter(
                        DefaultBlockParameter.valueOf("latest"),
                        DefaultBlockParameter.valueOf("latest"),
                        contractAddress.getValue()
                    );

                // Get logs synchronously and emit them
                EthLog ethLog = httpClient.ethGetLogs(filter).send();
                if (ethLog.hasError()) {
                    sink.error(new RuntimeException("Error getting contract logs: " + ethLog.getError().getMessage()));
                    return;
                }

                for (@SuppressWarnings("rawtypes") EthLog.LogResult logResult : ethLog.getLogs()) {
                    try {
                        org.web3j.protocol.core.methods.response.Log log =
                            (org.web3j.protocol.core.methods.response.Log) logResult.get();
                        ContractEvent event = convertToDomainContractEvent(log, network);
                        sink.next(event);
                    } catch (Exception e) {
                        sink.error(new RuntimeException("Failed to process contract event log", e));
                        return;
                    }
                }

                sink.complete();

            } catch (Exception e) {
                sink.error(new RuntimeException("Failed to setup contract event monitoring", e));
            }
        });
    }

    @Override
    public Mono<NetworkStatus> getNetworkStatus(Network network) {
        return Mono.zip(
            getLatestBlockNumber(network),
            Mono.fromCallable(() -> 1L), // Placeholder for active connections
            (latestBlock, connections) -> new NetworkStatus(true, latestBlock, connections)
        ).onErrorResume(e -> Mono.just(new NetworkStatus(false, BigInteger.ZERO, 0L)));
    }

    private Web3j getHttpClient(Network network) {
        return httpClients.computeIfAbsent(network.getName(), key -> {
            String rpcUrl = getRpcUrl(network);
            return Web3j.build(new org.web3j.protocol.http.HttpService(rpcUrl));
        });
    }

    private Web3j getWsClient(Network network) throws IOException {
        String key = network.getName();
        if (wsClients.containsKey(key)) {
            return wsClients.get(key);
        }

        String wsUrl = getWsUrl(network);
        try {
            WebSocketService wsService = new WebSocketService(wsUrl, true);
            wsService.connect();
            Web3j client = Web3j.build(wsService);
            wsClients.put(key, client);
            return client;
        } catch (Exception e) {
            throw new IOException("Failed to connect to WebSocket at " + wsUrl, e);
        }
    }

    private Mono<dev.bloco.domain.entity.Transaction> getTransactionByIndex(Address address, BigInteger index, Network network) {
        return Mono.fromFuture(() -> getHttpClient(network)
                .ethGetTransactionByBlockNumberAndIndex(DefaultBlockParameter.valueOf("latest"), index).sendAsync())
            .filter(ethTx -> ethTx.getTransaction().isPresent())
            .map(ethTx -> ethTx.getTransaction().get())
            .filter(tx -> address.getValue().equalsIgnoreCase(tx.getFrom()) ||
                         (tx.getTo() != null && address.getValue().equalsIgnoreCase(tx.getTo())))
            .map(tx -> convertToDomainTransaction(tx, network))
            .onErrorResume(IOException.class, e -> Mono.empty());
    }

    private Block convertToDomainBlock(org.web3j.protocol.core.methods.response.EthBlock.Block block, Network network) {
        return new Block(
            block.getNumber(),
            TransactionHash.of(block.getHash()),
            TransactionHash.of(block.getParentHash()),
            Address.of(block.getMiner()),
            Instant.ofEpochSecond(block.getTimestamp().longValue()),
            network,
            block.getTransactions().stream()
                .map(tx -> TransactionHash.of(tx.get().toString()))
                .toList(),
            block.getGasUsed(),
            block.getGasLimit(),
            true // Assume confirmed for now
        );
    }

    private dev.bloco.domain.entity.Transaction convertToDomainTransaction(Object tx, Network network) {
        // Cast to Web3j Transaction type
        if (!(tx instanceof org.web3j.protocol.core.methods.response.Transaction)) {
            throw new IllegalArgumentException("Expected Web3j Transaction type");
        }

        org.web3j.protocol.core.methods.response.Transaction web3jTx =
            (org.web3j.protocol.core.methods.response.Transaction) tx;

        // Get block information for timestamp
        Instant timestamp = Instant.now();
        if (web3jTx.getBlockNumber() != null) {
            try {
                timestamp = getBlockTimestamp(web3jTx.getBlockNumber(), network);
            } catch (Exception e) {
                // Fallback to current time if block timestamp cannot be retrieved
            }
        }

        return new dev.bloco.domain.entity.Transaction(
            TransactionHash.of(web3jTx.getHash()),
            Address.of(web3jTx.getFrom()),
            web3jTx.getTo() != null ? Address.of(web3jTx.getTo()) : null,
            network,
            TransactionValue.of(web3jTx.getValue()),
            web3jTx.getGasPrice(),
            web3jTx.getGas(),
            web3jTx.getGas(), // gasUsed - in a real implementation, this would be fetched from receipt
            web3jTx.getBlockNumber(),
            web3jTx.getBlockHash(),
            web3jTx.getTransactionIndex() != null ? web3jTx.getTransactionIndex().intValue() : 0,
            1, // status - would need receipt to determine actual status
            timestamp,
            web3jTx.getBlockNumber() != null // confirmed if block number exists
        );
    }

    private String getRpcUrl(Network network) {
        // This should be configurable - placeholder implementation
        return switch (network.getName()) {
            case "ethereum" -> "https://mainnet.infura.io/v3/YOUR_KEY";
            case "polygon" -> "https://polygon-mainnet.infura.io/v3/YOUR_KEY";
            default -> "https://localhost:8545";
        };
    }

    private Instant getBlockTimestamp(BigInteger blockNumber, Network network) throws IOException {
        EthBlock ethBlock = getHttpClient(network)
            .ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), false)
            .send();

        if (ethBlock.getBlock() != null) {
            return Instant.ofEpochSecond(ethBlock.getBlock().getTimestamp().longValue());
        }

        throw new IOException("Block not found: " + blockNumber);
    }

    private String getWsUrl(Network network) {
        // This should be configurable - placeholder implementation
        return switch (network.getName()) {
            case "ethereum" -> "wss://mainnet.infura.io/ws/v3/YOUR_KEY";
            case "polygon" -> "wss://polygon-mainnet.infura.io/ws/v3/YOUR_KEY";
            default -> "ws://localhost:8546";
        };
    }

    private ContractEvent convertToDomainContractEvent(org.web3j.protocol.core.methods.response.Log log, Network network) {
        try {
            Instant timestamp = getBlockTimestamp(log.getBlockNumber(), network);

            return new ContractEvent(
                TransactionHash.of(log.getTransactionHash()),
                Address.of(log.getAddress()),
                log.getTopics().isEmpty() ? "" : log.getTopics().get(0), // event signature
                log.getTopics(),
                log.getData(),
                timestamp,
                network,
                log.getLogIndex().intValue(),
                log.isRemoved()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert log to ContractEvent", e);
        }
    }
}