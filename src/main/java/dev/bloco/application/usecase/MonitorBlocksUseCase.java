package dev.bloco.application.usecase;

import dev.bloco.domain.entity.Block;
import dev.bloco.domain.repository.BlockRepository;
import dev.bloco.domain.service.BlockchainService;
import dev.bloco.domain.valueobject.Network;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

/**
 * Use Case for monitoring blockchain blocks.
 * Orchestrates the business logic for tracking block production and statistics.
 */
public class MonitorBlocksUseCase {

    private final BlockchainService blockchainService;
    private final BlockRepository blockRepository;

    public MonitorBlocksUseCase(
            BlockchainService blockchainService,
            BlockRepository blockRepository) {
        this.blockchainService = blockchainService;
        this.blockRepository = blockRepository;
    }

    /**
     * Monitor new blocks for a network
     */
    public Flux<Block> monitorBlocks(Network network) {
        return blockchainService.streamBlocks(network)
            .flatMap(block -> blockRepository.save(block));
    }

    /**
     * Get latest block for a network
     */
    public Mono<Block> getLatestBlock(Network network) {
        return blockRepository.findLatestBlock(network);
    }

    /**
     * Get blocks within a range
     */
    public Flux<Block> getBlocksInRange(Network network, BigInteger startBlock, BigInteger endBlock) {
        return blockRepository.findByNumberRange(startBlock, endBlock, network);
    }

    /**
     * Get recent blocks
     */
    public Flux<Block> getRecentBlocks(Network network, int minutes) {
        return blockRepository.findRecentBlocks(network, minutes);
    }

    /**
     * Get blocks with high gas utilization
     */
    public Flux<Block> getHighGasUtilizationBlocks(Network network) {
        return blockRepository.findHighGasUtilizationBlocks(network);
    }

    /**
     * Get network block statistics
     */
    public Mono<BlockStats> getNetworkBlockStats(Network network) {
        return blockRepository.getBlockStats(network)
            .map(stats -> new BlockStats(
                stats.getTotalBlocks(),
                stats.getLatestBlockNumber(),
                stats.getAverageGasUtilization(),
                stats.getUniqueMiners()
            ));
    }

    /**
     * Check if network is experiencing high congestion
     */
    public Mono<Boolean> isNetworkCongested(Network network) {
        return blockRepository.findRecentBlocks(network, 10) // Last 10 minutes
            .filter(Block::hasHighGasUtilization)
            .count()
            .map(count -> count >= 5); // 50% or more blocks have high gas utilization
    }

    /**
     * Get average block time for network
     */
    public Mono<Double> getAverageBlockTime(Network network) {
        return blockRepository.findRecentBlocks(network, 60) // Last hour
            .collectList()
            .map(blocks -> {
                if (blocks.size() < 2) return 0.0;
                // Calculate average time difference between consecutive blocks
                double totalTimeDiff = 0;
                for (int i = 1; i < blocks.size(); i++) {
                    long timeDiff = blocks.get(i).getTimestamp().getEpochSecond() -
                                   blocks.get(i-1).getTimestamp().getEpochSecond();
                    totalTimeDiff += timeDiff;
                }
                return totalTimeDiff / (blocks.size() - 1);
            });
    }

    /**
     * Block statistics
     */
    public static class BlockStats {
        private final long totalBlocks;
        private final BigInteger latestBlockNumber;
        private final double averageGasUtilization;
        private final long uniqueMiners;

        public BlockStats(long totalBlocks, BigInteger latestBlockNumber,
                         double averageGasUtilization, long uniqueMiners) {
            this.totalBlocks = totalBlocks;
            this.latestBlockNumber = latestBlockNumber;
            this.averageGasUtilization = averageGasUtilization;
            this.uniqueMiners = uniqueMiners;
        }

        public long getTotalBlocks() { return totalBlocks; }
        public BigInteger getLatestBlockNumber() { return latestBlockNumber; }
        public double getAverageGasUtilization() { return averageGasUtilization; }
        public long getUniqueMiners() { return uniqueMiners; }
    }
}