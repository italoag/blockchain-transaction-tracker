package dev.bloco.application.usecase;

import dev.bloco.domain.repository.BlockRepository;
import dev.bloco.domain.repository.SmartContractRepository;
import dev.bloco.domain.repository.TransactionRepository;
import dev.bloco.domain.repository.WalletAddressRepository;
import dev.bloco.domain.valueobject.Network;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Use Case for generating analytics and reports.
 * Orchestrates the business logic for data analysis and insights.
 */
public class GenerateAnalyticsUseCase {

    private final TransactionRepository transactionRepository;
    private final WalletAddressRepository walletRepository;
    private final SmartContractRepository contractRepository;
    private final BlockRepository blockRepository;

    public GenerateAnalyticsUseCase(
            TransactionRepository transactionRepository,
            WalletAddressRepository walletRepository,
            SmartContractRepository contractRepository,
            BlockRepository blockRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.contractRepository = contractRepository;
        this.blockRepository = blockRepository;
    }

    /**
     * Generate comprehensive network analytics
     */
    public Mono<NetworkAnalytics> generateNetworkAnalytics(Network network) {
        return Mono.zip(
            transactionRepository.getTransactionStats(),
            walletRepository.getWalletStats(),
            contractRepository.getContractStats(network),
            blockRepository.getBlockStats(network)
        ).map(tuple -> new NetworkAnalytics(
            tuple.getT1(),
            tuple.getT2(),
            tuple.getT3(),
            tuple.getT4()
        ));
    }

    /**
     * Generate system-wide analytics
     */
    public Mono<SystemAnalytics> generateSystemAnalytics() {
        return Mono.zip(
            transactionRepository.count(),
            walletRepository.getWalletStats(),
            contractRepository.findByNetwork(null).count(), // This would need to be implemented
            Mono.just(0L) // Placeholder for total blocks
        ).map(tuple -> new SystemAnalytics(
            tuple.getT1(),
            tuple.getT2().getTotalWallets(),
            tuple.getT3(),
            tuple.getT4()
        ));
    }

    /**
     * Get top active wallets by transaction count
     */
    public Flux<WalletActivity> getTopActiveWallets(int limit) {
        return walletRepository.findActiveWallets()
            .flatMap(wallet -> transactionRepository.countByAddress(wallet.getAddress())
                .map(count -> new WalletActivity(wallet, count)))
            .sort((a, b) -> Long.compare(b.getTransactionCount(), a.getTransactionCount()))
            .take(limit);
    }

    /**
     * Get network performance metrics
     */
    public Mono<NetworkPerformance> getNetworkPerformance(Network network) {
        return Mono.zip(
            blockRepository.getAverageGasUtilization(network),
            blockRepository.findRecentBlocks(network, 60).count(),
            transactionRepository.findRecentTransactions(60).count()
        ).map(tuple -> new NetworkPerformance(
            network,
            tuple.getT1(),
            tuple.getT2(),
            tuple.getT3()
        ));
    }

    /**
     * Network analytics data
     */
    public static class NetworkAnalytics {
        private final TransactionRepository.TransactionStats transactionStats;
        private final WalletAddressRepository.WalletStats walletStats;
        private final SmartContractRepository.ContractStats contractStats;
        private final BlockRepository.BlockStats blockStats;

        public NetworkAnalytics(TransactionRepository.TransactionStats transactionStats,
                              WalletAddressRepository.WalletStats walletStats,
                              SmartContractRepository.ContractStats contractStats,
                              BlockRepository.BlockStats blockStats) {
            this.transactionStats = transactionStats;
            this.walletStats = walletStats;
            this.contractStats = contractStats;
            this.blockStats = blockStats;
        }

        public TransactionRepository.TransactionStats getTransactionStats() { return transactionStats; }
        public WalletAddressRepository.WalletStats getWalletStats() { return walletStats; }
        public SmartContractRepository.ContractStats getContractStats() { return contractStats; }
        public BlockRepository.BlockStats getBlockStats() { return blockStats; }
    }

    /**
     * System-wide analytics data
     */
    public static class SystemAnalytics {
        private final long totalTransactions;
        private final long totalWallets;
        private final long totalContracts;
        private final long totalBlocks;

        public SystemAnalytics(long totalTransactions, long totalWallets,
                             long totalContracts, long totalBlocks) {
            this.totalTransactions = totalTransactions;
            this.totalWallets = totalWallets;
            this.totalContracts = totalContracts;
            this.totalBlocks = totalBlocks;
        }

        public long getTotalTransactions() { return totalTransactions; }
        public long getTotalWallets() { return totalWallets; }
        public long getTotalContracts() { return totalContracts; }
        public long getTotalBlocks() { return totalBlocks; }
    }

    /**
     * Wallet activity data
     */
    public static class WalletActivity {
        private final dev.bloco.domain.entity.WalletAddress wallet;
        private final long transactionCount;

        public WalletActivity(dev.bloco.domain.entity.WalletAddress wallet, long transactionCount) {
            this.wallet = wallet;
            this.transactionCount = transactionCount;
        }

        public dev.bloco.domain.entity.WalletAddress getWallet() { return wallet; }
        public long getTransactionCount() { return transactionCount; }
    }

    /**
     * Network performance metrics
     */
    public static class NetworkPerformance {
        private final Network network;
        private final double averageGasUtilization;
        private final long blocksPerHour;
        private final long transactionsPerHour;

        public NetworkPerformance(Network network, double averageGasUtilization,
                                long blocksPerHour, long transactionsPerHour) {
            this.network = network;
            this.averageGasUtilization = averageGasUtilization;
            this.blocksPerHour = blocksPerHour;
            this.transactionsPerHour = transactionsPerHour;
        }

        public Network getNetwork() { return network; }
        public double getAverageGasUtilization() { return averageGasUtilization; }
        public long getBlocksPerHour() { return blocksPerHour; }
        public long getTransactionsPerHour() { return transactionsPerHour; }
    }
}