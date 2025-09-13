package dev.bloco.presentation.dto;

import dev.bloco.domain.repository.TransactionRepository;

import java.time.Instant;

/**
 * DTO for transaction statistics.
 * Used for API responses.
 */
public record TransactionStatsDto(
    long totalTransactions,
    long uniqueAddresses,
    long totalNetworks,
    Instant oldestTransaction,
    Instant newestTransaction
) {

    /**
     * Convert domain stats to DTO
     */
    public static TransactionStatsDto fromDomain(TransactionRepository.TransactionStats stats) {
        return new TransactionStatsDto(
            stats.getTotalTransactions(),
            stats.getUniqueAddresses(),
            stats.getTotalNetworks(),
            stats.getOldestTransaction(),
            stats.getNewestTransaction()
        );
    }
}