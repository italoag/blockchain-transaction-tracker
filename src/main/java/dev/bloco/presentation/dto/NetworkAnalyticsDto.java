package dev.bloco.presentation.dto;

import dev.bloco.application.usecase.GenerateAnalyticsUseCase;

/**
 * DTO for network analytics.
 * Used for API responses.
 */
public record NetworkAnalyticsDto(
    TransactionStatsDto transactionStats,
    WalletStatsDto walletStats,
    ContractStatsDto contractStats,
    BlockStatsDto blockStats
) {

    /**
     * Convert domain analytics to DTO
     */
    public static NetworkAnalyticsDto fromDomain(GenerateAnalyticsUseCase.NetworkAnalytics analytics) {
        return new NetworkAnalyticsDto(
            TransactionStatsDto.fromDomain(analytics.getTransactionStats()),
            WalletStatsDto.fromDomain(analytics.getWalletStats()),
            ContractStatsDto.fromDomain(analytics.getContractStats()),
            BlockStatsDto.fromDomain(analytics.getBlockStats())
        );
    }
}