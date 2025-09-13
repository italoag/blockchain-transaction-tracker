package dev.bloco.infrastructure.repository;

import dev.bloco.domain.entity.Transaction;
import dev.bloco.domain.repository.TransactionRepository;
import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import dev.bloco.domain.valueobject.TransactionHash;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory implementation of TransactionRepository.
 * Thread-safe implementation using ConcurrentHashMap for storage.
 */
public class InMemoryTransactionRepository implements TransactionRepository {

    private final ConcurrentMap<String, Transaction> storage = new ConcurrentHashMap<>();

    @Override
    public Mono<Transaction> save(Transaction transaction) {
        return Mono.fromCallable(() -> {
            String key = createKey(transaction.getHash(), transaction.getNetwork());
            storage.put(key, transaction);
            return transaction;
        });
    }

    @Override
    public Flux<Transaction> saveAll(Flux<Transaction> transactions) {
        return transactions.flatMap(this::save);
    }

    @Override
    public Mono<Transaction> findByHashAndNetwork(TransactionHash hash, Network network) {
        return Mono.fromCallable(() -> {
            String key = createKey(hash, network);
            return storage.get(key);
        });
    }

    @Override
    public Flux<Transaction> findByNetwork(Network network) {
        return Flux.fromIterable(storage.values())
            .filter(tx -> tx.getNetwork().equals(network));
    }

    @Override
    public Flux<Transaction> findByAddress(Address address) {
        return Flux.fromIterable(storage.values())
            .filter(tx -> tx.involvesAddress(address));
    }

    @Override
    public Flux<Transaction> findByAddressAndNetwork(Address address, Network network) {
        return Flux.fromIterable(storage.values())
            .filter(tx -> tx.getNetwork().equals(network) && tx.involvesAddress(address));
    }

    @Override
    public Flux<Transaction> findByTimeRange(Instant startTime, Instant endTime) {
        return Flux.fromIterable(storage.values())
            .filter(tx -> {
                Instant txTime = tx.getTimestamp();
                return txTime.isAfter(startTime) && txTime.isBefore(endTime);
            });
    }

    @Override
    public Flux<Transaction> findByAddressAndTimeRange(Address address, Instant startTime, Instant endTime) {
        return Flux.fromIterable(storage.values())
            .filter(tx -> tx.involvesAddress(address))
            .filter(tx -> {
                Instant txTime = tx.getTimestamp();
                return txTime.isAfter(startTime) && txTime.isBefore(endTime);
            });
    }

    @Override
    public Flux<Transaction> findRecentTransactions(int minutes) {
        Instant cutoffTime = Instant.now().minusSeconds(minutes * 60L);
        return Flux.fromIterable(storage.values())
            .filter(tx -> tx.getTimestamp().isAfter(cutoffTime));
    }

    @Override
    public Flux<Transaction> findRecentTransactionsByAddress(Address address, int minutes) {
        Instant cutoffTime = Instant.now().minusSeconds(minutes * 60L);
        return Flux.fromIterable(storage.values())
            .filter(tx -> tx.involvesAddress(address) && tx.getTimestamp().isAfter(cutoffTime));
    }

    @Override
    public Mono<Long> count() {
        return Mono.fromCallable(() -> (long) storage.size());
    }

    @Override
    public Mono<Long> countByNetwork(Network network) {
        return Flux.fromIterable(storage.values())
            .filter(tx -> tx.getNetwork().equals(network))
            .count();
    }

    @Override
    public Mono<Long> countByAddress(Address address) {
        return Flux.fromIterable(storage.values())
            .filter(tx -> tx.involvesAddress(address))
            .count();
    }

    @Override
    public Mono<Long> countByAddressAndNetwork(Address address, Network network) {
        return Flux.fromIterable(storage.values())
            .filter(tx -> tx.getNetwork().equals(network) && tx.involvesAddress(address))
            .count();
    }

    @Override
    public Mono<Boolean> existsByHashAndNetwork(TransactionHash hash, Network network) {
        return Mono.fromCallable(() -> {
            String key = createKey(hash, network);
            return storage.containsKey(key);
        });
    }

    @Override
    public Mono<Void> deleteByHashAndNetwork(TransactionHash hash, Network network) {
        return Mono.fromRunnable(() -> {
            String key = createKey(hash, network);
            storage.remove(key);
        });
    }

    @Override
    public Mono<Long> deleteOlderThan(int minutes) {
        Instant cutoffTime = Instant.now().minusSeconds(minutes * 60L);
        return Flux.fromIterable(storage.entrySet())
            .filter(entry -> entry.getValue().getTimestamp().isBefore(cutoffTime))
            .map(entry -> entry.getKey())
            .collectList()
            .flatMap(keys -> {
                keys.forEach(storage::remove);
                return Mono.just((long) keys.size());
            });
    }

    @Override
    public Mono<TransactionStats> getTransactionStats() {
        return Flux.fromIterable(storage.values())
            .collectList()
            .map(transactions -> {
                long totalTransactions = transactions.size();
                long uniqueAddresses = transactions.stream()
                    .flatMap(tx -> java.util.List.of(tx.getFrom(), tx.getTo()).stream())
                    .distinct()
                    .count();
                long totalNetworks = transactions.stream()
                    .map(Transaction::getNetwork)
                    .distinct()
                    .count();
                Instant oldestTransaction = transactions.stream()
                    .map(Transaction::getTimestamp)
                    .min(Instant::compareTo)
                    .orElse(Instant.now());
                Instant newestTransaction = transactions.stream()
                    .map(Transaction::getTimestamp)
                    .max(Instant::compareTo)
                    .orElse(Instant.now());

                return new TransactionStats(totalTransactions, uniqueAddresses,
                                          totalNetworks, oldestTransaction, newestTransaction);
            });
    }

    private String createKey(TransactionHash hash, Network network) {
        return network.getName() + ":" + hash.getValue();
    }
}