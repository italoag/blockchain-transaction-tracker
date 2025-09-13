package dev.bloco.presentation.dto;

import dev.bloco.application.usecase.MonitorContractEventsUseCase;

/**
 * DTO for contract event statistics.
 * Used for API responses.
 */
public record ContractEventStatsDto(
    long totalEvents,
    long recentEvents,
    long transferEvents
) {

    /**
     * Convert domain stats to DTO
     */
    public static ContractEventStatsDto fromDomain(MonitorContractEventsUseCase.ContractEventStats stats) {
        return new ContractEventStatsDto(
            stats.getTotalEvents(),
            stats.getRecentEvents(),
            stats.getTransferEvents()
        );
    }
}