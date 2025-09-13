package dev.bloco.domain.repository;

import dev.bloco.domain.entity.Transaction;
import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import dev.bloco.domain.valueobject.TransactionHash;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Domain Repository interface for transaction persistence operations.
 * Defines the contract for transaction data access following DDD principles.
 */
public interface TransactionRepository {

    /**
     * Save a single transaction
     */
    Mono<Transaction> save(Transaction transaction);

    /**
     * Save multiple transactions
     */
    Flux<Transaction> saveAll(Flux<Transaction> transactions);

    /**
     * Find transaction by hash and network
     */
    Mono<Transaction> findByHashAndNetwork(TransactionHash hash, Network network);

    /**
     * Find all transactions for a network
     */
    Flux<Transaction> findByNetwork(Network network);

    /**
     * Find all transactions for an address
     */
    Flux<Transaction> findByAddress(Address address);

    /**
     * Find transactions for an address on a specific network
     */
    Flux<Transaction> findByAddressAndNetwork(Address address, Network network);

    /**
     * Find transactions within a time range
     */
    Flux<Transaction> findByTimeRange(Instant startTime, Instant endTime);

    /**
     * Find transactions for an address within a time range
     */
    Flux<Transaction> findByAddressAndTimeRange(Address address, Instant startTime, Instant endTime);

    /**
     * Find recent transactions (last N minutes)
     */
    Flux<Transaction> findRecentTransactions(int minutes);

    /**
     * Find recent transactions for an address
     */
    Flux<Transaction> findRecentTransactionsByAddress(Address address, int minutes);

    /**
     * Check if transaction exists
     */
    Mono<Boolean> existsByHashAndNetwork(TransactionHash hash, Network network);

    /**
     * Count total transactions
     */
    Mono<Long> count();

    /**
     * Count transactions for a network
     */
    Mono<Long> countByNetwork(Network network);

    /**
     * Count transactions for an address
     */
    Mono<Long> countByAddress(Address address);

    /**
     * Count transactions for an address on a network
     */
    Mono<Long> countByAddressAndNetwork(Address address, Network network);

    /**
     * Delete transaction by hash and network
     */
    Mono<Void> deleteByHashAndNetwork(TransactionHash hash, Network network);

    /**
     * Delete old transactions (older than specified minutes)
     */
    Mono<Long> deleteOlderThan(int minutes);

    /**
     * Get transaction statistics
     */
    Mono<TransactionStats> getTransactionStats();

    /**
     * Transaction statistics
     */
    class TransactionStats {
        private final long totalTransactions;
        private final long uniqueAddresses;
        private final long totalNetworks;
        private final Instant oldestTransaction;
        private final Instant newestTransaction;

        public TransactionStats(long totalTransactions, long uniqueAddresses,
                              long totalNetworks, Instant oldestTransaction,
                              Instant newestTransaction) {
            this.totalTransactions = totalTransactions;
            this.uniqueAddresses = uniqueAddresses;
            this.totalNetworks = totalNetworks;
            this.oldestTransaction = oldestTransaction;
            this.newestTransaction = newestTransaction;
        }

        public long getTotalTransactions() { return totalTransactions; }
        public long getUniqueAddresses() { return uniqueAddresses; }
        public long getTotalNetworks() { return totalNetworks; }
        public Instant getOldestTransaction() { return oldestTransaction; }
        public Instant getNewestTransaction() { return newestTransaction; }
    }
}