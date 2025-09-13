package dev.bloco.infrastructure.repository;

import dev.bloco.domain.entity.WalletAddress;
import dev.bloco.domain.repository.WalletAddressRepository;
import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory implementation of WalletAddressRepository.
 * Thread-safe implementation using ConcurrentHashMap for storage.
 */
public class InMemoryWalletAddressRepository implements WalletAddressRepository {

    private final ConcurrentMap<String, WalletAddress> storage = new ConcurrentHashMap<>();

    @Override
    public Mono<WalletAddress> save(WalletAddress walletAddress) {
        return Mono.fromCallable(() -> {
            String key = createKey(walletAddress.getAddress(), walletAddress.getNetwork());
            storage.put(key, walletAddress);
            return walletAddress;
        });
    }

    @Override
    public Mono<WalletAddress> findByAddressAndNetwork(Address address, Network network) {
        return Mono.fromCallable(() -> {
            String key = createKey(address, network);
            return storage.get(key);
        });
    }

    @Override
    public Flux<WalletAddress> findByNetwork(Network network) {
        return Flux.fromIterable(storage.values())
            .filter(wallet -> wallet.getNetwork().equals(network));
    }

    @Override
    public Flux<WalletAddress> findByType(WalletAddress.WalletType type) {
        return Flux.fromIterable(storage.values())
            .filter(wallet -> wallet.getWalletType().equals(type));
    }

    @Override
    public Flux<WalletAddress> findByPriority(WalletAddress.Priority priority) {
        return Flux.fromIterable(storage.values())
            .filter(wallet -> wallet.getPriority().equals(priority));
    }

    @Override
    public Flux<WalletAddress> findByCreationTimeRange(java.time.Instant startTime, java.time.Instant endTime) {
        return Flux.fromIterable(storage.values())
            .filter(wallet -> {
                java.time.Instant createdAt = wallet.getCreatedAt();
                return createdAt.isAfter(startTime) && createdAt.isBefore(endTime);
            });
    }

    @Override
    public Flux<WalletAddress> findActiveWallets() {
        return Flux.fromIterable(storage.values())
            .filter(WalletAddress::isActive);
    }

    @Override
    public Flux<WalletAddress> findHighPriorityWallets() {
        return Flux.fromIterable(storage.values())
            .filter(wallet -> wallet.getPriority().equals(WalletAddress.Priority.HIGH));
    }

    @Override
    public Mono<Boolean> existsByAddressAndNetwork(Address address, Network network) {
        return Mono.fromCallable(() -> {
            String key = createKey(address, network);
            return storage.containsKey(key);
        });
    }

    @Override
    public Mono<Long> countByNetwork(Network network) {
        return Flux.fromIterable(storage.values())
            .filter(wallet -> wallet.getNetwork().equals(network))
            .count();
    }

    @Override
    public Mono<Long> countByType(WalletAddress.WalletType type) {
        return Flux.fromIterable(storage.values())
            .filter(wallet -> wallet.getWalletType().equals(type))
            .count();
    }

    @Override
    public Mono<Long> countByPriority(WalletAddress.Priority priority) {
        return Flux.fromIterable(storage.values())
            .filter(wallet -> wallet.getPriority().equals(priority))
            .count();
    }

    @Override
    public Mono<WalletAddress> updatePriority(Address address, Network network, WalletAddress.Priority newPriority) {
        return Mono.fromCallable(() -> {
            String key = createKey(address, network);
            WalletAddress existing = storage.get(key);
            if (existing != null) {
                WalletAddress updated = new WalletAddress(
                    existing.getAddress(),
                    existing.getNetwork(),
                    existing.getLabel(),
                    existing.getWalletType(),
                    newPriority
                );
                storage.put(key, updated);
                return updated;
            }
            return null;
        });
    }

    @Override
    public Mono<Void> deactivateWallet(Address address, Network network) {
        return Mono.fromRunnable(() -> {
            String key = createKey(address, network);
            WalletAddress existing = storage.get(key);
            if (existing != null) {
                existing.deactivate();
                storage.put(key, existing);
            }
        });
    }

    @Override
    public Mono<Void> reactivateWallet(Address address, Network network) {
        return Mono.fromRunnable(() -> {
            String key = createKey(address, network);
            WalletAddress existing = storage.get(key);
            if (existing != null) {
                existing.reactivate();
                storage.put(key, existing);
            }
        });
    }

    @Override
    public Mono<Void> deleteByAddressAndNetwork(Address address, Network network) {
        return Mono.fromRunnable(() -> {
            String key = createKey(address, network);
            storage.remove(key);
        });
    }

    @Override
    public Mono<WalletStats> getWalletStats() {
        return Flux.fromIterable(storage.values())
            .collectList()
            .map(wallets -> {
                long totalWallets = wallets.size();
                long activeWallets = wallets.stream().mapToLong(wallet -> wallet.isActive() ? 1 : 0).sum();
                long highPriorityWallets = wallets.stream()
                    .mapToLong(wallet -> wallet.getPriority().equals(WalletAddress.Priority.HIGH) ? 1 : 0)
                    .sum();
                long exchangeWallets = wallets.stream()
                    .mapToLong(wallet -> wallet.getWalletType().equals(WalletAddress.WalletType.EXCHANGE) ? 1 : 0)
                    .sum();
                long defiWallets = wallets.stream()
                    .mapToLong(wallet -> wallet.getWalletType().equals(WalletAddress.WalletType.DEFI) ? 1 : 0)
                    .sum();
                long whaleWallets = wallets.stream()
                    .mapToLong(wallet -> wallet.getWalletType().equals(WalletAddress.WalletType.WHALE) ? 1 : 0)
                    .sum();

                return new WalletStats(totalWallets, activeWallets, highPriorityWallets,
                                     exchangeWallets, defiWallets, whaleWallets);
            });
    }

    private String createKey(Address address, Network network) {
        return network.getName() + ":" + address.getValue();
    }
}