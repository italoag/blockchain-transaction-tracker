package dev.bloco.domain.service;

import dev.bloco.domain.entity.Transaction;
import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

/**
 * Domain Service interface for blockchain operations.
 * Defines the contract for blockchain connectivity and data retrieval.
 */
public interface BlockchainService {

    /**
     * Get latest block number for a network
     */
    Mono<BigInteger> getLatestBlockNumber(Network network);

    /**
     * Stream new blocks for a network
     */
    Flux<dev.bloco.domain.entity.Block> streamBlocks(Network network);

    /**
     * Get transactions for an address on a network
     */
    Flux<Transaction> getTransactionsForAddress(Address address, Network network);

    /**
     * Get transaction by hash
     */
    Mono<Transaction> getTransactionByHash(dev.bloco.domain.valueobject.TransactionHash hash, Network network);

    /**
     * Check if address is a contract
     */
    Mono<Boolean> isContractAddress(Address address, Network network);

    /**
     * Get contract events
     */
    Flux<dev.bloco.domain.entity.ContractEvent> getContractEvents(Address contractAddress, Network network);

    /**
     * Get network status
     */
    Mono<NetworkStatus> getNetworkStatus(Network network);

    /**
     * Network status information
     */
    class NetworkStatus {
        private final boolean connected;
        private final BigInteger latestBlock;
        private final long activeConnections;

        public NetworkStatus(boolean connected, BigInteger latestBlock, long activeConnections) {
            this.connected = connected;
            this.latestBlock = latestBlock;
            this.activeConnections = activeConnections;
        }

        public boolean isConnected() { return connected; }
        public BigInteger getLatestBlock() { return latestBlock; }
        public long getActiveConnections() { return activeConnections; }
    }
}