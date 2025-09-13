package dev.bloco.presentation.dto;

import dev.bloco.domain.repository.SmartContractRepository;

import java.time.Instant;

/**
 * DTO for contract statistics.
 * Used for API responses.
 */
public record ContractStatsDto(
    long totalContracts,
    long totalEvents,
    long erc20Contracts,
    long erc721Contracts,
    Instant oldestContract,
    Instant newestContract
) {

    /**
     * Convert domain stats to DTO
     */
    public static ContractStatsDto fromDomain(SmartContractRepository.ContractStats stats) {
        return new ContractStatsDto(
            stats.getTotalContracts(),
            stats.getTotalEvents(),
            stats.getErc20Contracts(),
            stats.getErc721Contracts(),
            stats.getOldestContract(),
            stats.getNewestContract()
        );
    }
}