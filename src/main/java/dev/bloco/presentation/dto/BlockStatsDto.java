package dev.bloco.presentation.dto;

import dev.bloco.domain.repository.BlockRepository;

import java.math.BigInteger;
import java.time.Instant;

/**
 * DTO for block statistics.
 * Used for API responses.
 */
public record BlockStatsDto(
    long totalBlocks,
    BigInteger latestBlockNumber,
    double averageGasUtilization,
    long uniqueMiners,
    Instant oldestBlock,
    Instant newestBlock
) {

    /**
     * Convert domain stats to DTO
     */
    public static BlockStatsDto fromDomain(BlockRepository.BlockStats stats) {
        return new BlockStatsDto(
            stats.getTotalBlocks(),
            stats.getLatestBlockNumber(),
            stats.getAverageGasUtilization(),
            stats.getUniqueMiners(),
            stats.getOldestBlock(),
            stats.getNewestBlock()
        );
    }
}