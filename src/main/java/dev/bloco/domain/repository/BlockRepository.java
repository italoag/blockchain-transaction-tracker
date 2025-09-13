package dev.bloco.domain.repository;

import dev.bloco.domain.entity.Block;
import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import dev.bloco.domain.valueobject.TransactionHash;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.time.Instant;

/**
 * Domain Repository interface for block persistence.
 * Defines the contract for block data access operations.
 */
public interface BlockRepository {

    /**
     * Save a block
     */
    Mono<Block> save(Block block);

    /**
     * Find block by number and network
     */
    Mono<Block> findByNumberAndNetwork(BigInteger number, Network network);

    /**
     * Find block by hash and network
     */
    Mono<Block> findByHashAndNetwork(TransactionHash hash, Network network);

    /**
     * Find latest block for a network
     */
    Mono<Block> findLatestBlock(Network network);

    /**
     * Find blocks within number range
     */
    Flux<Block> findByNumberRange(BigInteger startNumber, BigInteger endNumber, Network network);

    /**
     * Find blocks by miner address
     */
    Flux<Block> findByMiner(Address miner, Network network);

    /**
     * Find blocks within time range
     */
    Flux<Block> findByTimeRange(Instant startTime, Instant endTime, Network network);

    /**
     * Find recent blocks (last N minutes)
     */
    Flux<Block> findRecentBlocks(Network network, int minutes);

    /**
     * Find blocks with high gas utilization (>90%)
     */
    Flux<Block> findHighGasUtilizationBlocks(Network network);

    /**
     * Find blocks containing specific transaction
     */
    Flux<Block> findBlocksContainingTransaction(TransactionHash transactionHash, Network network);

    /**
     * Check if block exists
     */
    Mono<Boolean> existsByNumberAndNetwork(BigInteger number, Network network);

    /**
     * Count blocks for a network
     */
    Mono<Long> countByNetwork(Network network);

    /**
     * Count blocks by miner
     */
    Mono<Long> countByMiner(Address miner, Network network);

    /**
     * Get average gas utilization for network
     */
    Mono<Double> getAverageGasUtilization(Network network);

    /**
     * Get average gas utilization for time period
     */
    Mono<Double> getAverageGasUtilization(Network network, Instant startTime, Instant endTime);

    /**
     * Get block statistics
     */
    Mono<BlockStats> getBlockStats(Network network);

    /**
     * Delete block by number and network
     */
    Mono<Void> deleteByNumberAndNetwork(BigInteger number, Network network);

    /**
     * Delete old blocks (older than specified minutes)
     */
    Mono<Long> deleteOlderThan(int minutes, Network network);

    /**
     * Block statistics
     */
    class BlockStats {
        private final long totalBlocks;
        private final BigInteger latestBlockNumber;
        private final double averageGasUtilization;
        private final long uniqueMiners;
        private final Instant oldestBlock;
        private final Instant newestBlock;

        public BlockStats(long totalBlocks, BigInteger latestBlockNumber, double averageGasUtilization,
                         long uniqueMiners, Instant oldestBlock, Instant newestBlock) {
            this.totalBlocks = totalBlocks;
            this.latestBlockNumber = latestBlockNumber;
            this.averageGasUtilization = averageGasUtilization;
            this.uniqueMiners = uniqueMiners;
            this.oldestBlock = oldestBlock;
            this.newestBlock = newestBlock;
        }

        public long getTotalBlocks() { return totalBlocks; }
        public BigInteger getLatestBlockNumber() { return latestBlockNumber; }
        public double getAverageGasUtilization() { return averageGasUtilization; }
        public long getUniqueMiners() { return uniqueMiners; }
        public Instant getOldestBlock() { return oldestBlock; }
        public Instant getNewestBlock() { return newestBlock; }
    }
}