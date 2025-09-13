package dev.bloco.domain.repository;

import dev.bloco.domain.entity.WalletAddress;
import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Domain Repository interface for wallet address persistence.
 * Defines the contract for wallet address data access operations.
 */
public interface WalletAddressRepository {

    /**
     * Save a wallet address
     */
    Mono<WalletAddress> save(WalletAddress walletAddress);

    /**
     * Find wallet by address and network
     */
    Mono<WalletAddress> findByAddressAndNetwork(Address address, Network network);

    /**
     * Find all wallets for a network
     */
    Flux<WalletAddress> findByNetwork(Network network);

    /**
     * Find wallets by type
     */
    Flux<WalletAddress> findByType(WalletAddress.WalletType type);

    /**
     * Find wallets by priority level
     */
    Flux<WalletAddress> findByPriority(WalletAddress.Priority priority);

    /**
     * Find wallets created within time range
     */
    Flux<WalletAddress> findByCreationTimeRange(Instant startTime, Instant endTime);

    /**
     * Find active wallets (being monitored)
     */
    Flux<WalletAddress> findActiveWallets();

    /**
     * Find high priority wallets
     */
    Flux<WalletAddress> findHighPriorityWallets();

    /**
     * Check if wallet exists
     */
    Mono<Boolean> existsByAddressAndNetwork(Address address, Network network);

    /**
     * Count wallets for a network
     */
    Mono<Long> countByNetwork(Network network);

    /**
     * Count wallets by type
     */
    Mono<Long> countByType(WalletAddress.WalletType type);

    /**
     * Count wallets by priority
     */
    Mono<Long> countByPriority(WalletAddress.Priority priority);

    /**
     * Update wallet priority
     */
    Mono<WalletAddress> updatePriority(Address address, Network network, WalletAddress.Priority newPriority);

    /**
     * Deactivate wallet monitoring
     */
    Mono<Void> deactivateWallet(Address address, Network network);

    /**
     * Reactivate wallet monitoring
     */
    Mono<Void> reactivateWallet(Address address, Network network);

    /**
     * Delete wallet by address and network
     */
    Mono<Void> deleteByAddressAndNetwork(Address address, Network network);

    /**
     * Get wallet statistics
     */
    Mono<WalletStats> getWalletStats();

    /**
     * Wallet statistics
     */
    class WalletStats {
        private final long totalWallets;
        private final long activeWallets;
        private final long highPriorityWallets;
        private final long exchangeWallets;
        private final long defiWallets;
        private final long whaleWallets;

        public WalletStats(long totalWallets, long activeWallets, long highPriorityWallets,
                         long exchangeWallets, long defiWallets, long whaleWallets) {
            this.totalWallets = totalWallets;
            this.activeWallets = activeWallets;
            this.highPriorityWallets = highPriorityWallets;
            this.exchangeWallets = exchangeWallets;
            this.defiWallets = defiWallets;
            this.whaleWallets = whaleWallets;
        }

        public long getTotalWallets() { return totalWallets; }
        public long getActiveWallets() { return activeWallets; }
        public long getHighPriorityWallets() { return highPriorityWallets; }
        public long getExchangeWallets() { return exchangeWallets; }
        public long getDefiWallets() { return defiWallets; }
        public long getWhaleWallets() { return whaleWallets; }
    }
}