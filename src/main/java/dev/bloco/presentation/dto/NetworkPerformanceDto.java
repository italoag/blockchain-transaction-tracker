package dev.bloco.presentation.dto;

import dev.bloco.application.usecase.GenerateAnalyticsUseCase;

/**
 * DTO for network performance metrics.
 * Used for API responses.
 */
public record NetworkPerformanceDto(
    String network,
    double averageGasUtilization,
    long blocksPerHour,
    long transactionsPerHour
) {

    /**
     * Convert domain performance metrics to DTO
     */
    public static NetworkPerformanceDto fromDomain(GenerateAnalyticsUseCase.NetworkPerformance performance) {
        return new NetworkPerformanceDto(
            performance.getNetwork().getName(),
            performance.getAverageGasUtilization(),
            performance.getBlocksPerHour(),
            performance.getTransactionsPerHour()
        );
    }
}