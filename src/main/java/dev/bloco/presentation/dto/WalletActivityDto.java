package dev.bloco.presentation.dto;

import dev.bloco.application.usecase.GenerateAnalyticsUseCase;

/**
 * DTO for wallet activity.
 * Used for API responses.
 */
public record WalletActivityDto(
    String address,
    String network,
    String label,
    long transactionCount
) {

    /**
     * Convert domain wallet activity to DTO
     */
    public static WalletActivityDto fromDomain(GenerateAnalyticsUseCase.WalletActivity activity) {
        return new WalletActivityDto(
            activity.getWallet().getAddress().getValue(),
            activity.getWallet().getNetwork().getName(),
            activity.getWallet().getLabel(),
            activity.getTransactionCount()
        );
    }
}