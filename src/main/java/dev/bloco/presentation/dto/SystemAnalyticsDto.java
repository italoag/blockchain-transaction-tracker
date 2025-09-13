package dev.bloco.presentation.dto;

import dev.bloco.application.usecase.GenerateAnalyticsUseCase;

/**
 * DTO for system analytics.
 * Used for API responses.
 */
public record SystemAnalyticsDto(
    long totalTransactions,
    long totalWallets,
    long totalContracts,
    long totalBlocks
) {

    /**
     * Convert domain analytics to DTO
     */
    public static SystemAnalyticsDto fromDomain(GenerateAnalyticsUseCase.SystemAnalytics analytics) {
        return new SystemAnalyticsDto(
            analytics.getTotalTransactions(),
            analytics.getTotalWallets(),
            analytics.getTotalContracts(),
            analytics.getTotalBlocks()
        );
    }
}