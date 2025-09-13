package dev.bloco.application.usecase;

import dev.bloco.domain.entity.Transaction;
import dev.bloco.domain.repository.TransactionRepository;
import dev.bloco.domain.repository.WalletAddressRepository;
import dev.bloco.domain.service.BlockchainService;
import dev.bloco.domain.service.DeduplicationService;
import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import dev.bloco.domain.valueobject.TransactionValue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigInteger;

/**
 * Use Case for tracking wallet transactions.
 * Orchestrates the business logic for monitoring wallet activity.
 */
public class TrackWalletTransactionsUseCase {

    private final BlockchainService blockchainService;
    private final TransactionRepository transactionRepository;
    private final WalletAddressRepository walletRepository;
    private final DeduplicationService deduplicationService;

    public TrackWalletTransactionsUseCase(
            BlockchainService blockchainService,
            TransactionRepository transactionRepository,
            WalletAddressRepository walletRepository,
            DeduplicationService deduplicationService) {
        this.blockchainService = blockchainService;
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.deduplicationService = deduplicationService;
    }

    /**
     * Track transactions for a specific wallet address
     */
    public Flux<Transaction> trackWallet(Address walletAddress, Network network) {
        return walletRepository.findByAddressAndNetwork(walletAddress, network)
            .flatMapMany(wallet -> {
                if (!wallet.isActive()) {
                    return Flux.empty();
                }
                return blockchainService.getTransactionsForAddress(walletAddress, network)
                    .filter(tx -> !deduplicationService.mightContain(network.getName(), tx.getHash().getValue()))
                    .flatMap(tx -> {
                        deduplicationService.add(network.getName(), tx.getHash().getValue());
                        return transactionRepository.save(tx);
                    });
            });
    }

    /**
     * Track transactions for all active wallets
     */
    public Flux<Transaction> trackAllActiveWallets() {
        return walletRepository.findActiveWallets()
            .flatMap(wallet -> trackWallet(wallet.getAddress(), wallet.getNetwork()));
    }

    /**
     * Track transactions for high priority wallets
     */
    public Flux<Transaction> trackHighPriorityWallets() {
        return walletRepository.findHighPriorityWallets()
            .flatMap(wallet -> trackWallet(wallet.getAddress(), wallet.getNetwork()));
    }

    /**
     * Get recent transactions for a wallet
     */
    public Flux<Transaction> getRecentWalletTransactions(Address walletAddress, Network network, int minutes) {
        return transactionRepository.findRecentTransactionsByAddress(walletAddress, minutes)
            .filter(tx -> tx.getNetwork().equals(network));
    }

    /**
     * Get wallet transaction statistics
     */
    public Mono<WalletTransactionStats> getWalletStats(Address walletAddress, Network network) {
        return Mono.zip(
            transactionRepository.countByAddressAndNetwork(walletAddress, network),
            transactionRepository.findRecentTransactionsByAddress(walletAddress, 60).count(),
            transactionRepository.findByAddressAndNetwork(walletAddress, network)
                .map(Transaction::getValue)
                .map(TransactionValue::getWei)
                .reduce(BigInteger::add)
                .defaultIfEmpty(BigInteger.ZERO)
        ).map(tuple -> new WalletTransactionStats(
            tuple.getT1(),
            tuple.getT2(),
            tuple.getT3()
        ));
    }

    /**
     * Wallet transaction statistics
     */
    public static class WalletTransactionStats {
        private final long totalTransactions;
        private final long recentTransactions;
        private final BigInteger totalValue;

        public WalletTransactionStats(long totalTransactions, long recentTransactions, BigInteger totalValue) {
            this.totalTransactions = totalTransactions;
            this.recentTransactions = recentTransactions;
            this.totalValue = totalValue;
        }

        public long getTotalTransactions() { return totalTransactions; }
        public long getRecentTransactions() { return recentTransactions; }
        public BigInteger getTotalValue() { return totalValue; }
    }
}