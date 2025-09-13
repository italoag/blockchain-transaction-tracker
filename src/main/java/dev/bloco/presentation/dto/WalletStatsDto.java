package dev.bloco.presentation.dto;

import dev.bloco.application.usecase.TrackWalletTransactionsUseCase;
import dev.bloco.domain.repository.WalletAddressRepository;

import java.math.BigInteger;

/**
 * DTO for wallet statistics.
 * Used for API responses.
 */
public record WalletStatsDto(
    long totalWallets,
    long activeWallets,
    long highPriorityWallets,
    long exchangeWallets,
    long defiWallets,
    long whaleWallets
) {

    /**
     * Convert domain wallet transaction stats to DTO
     */
    public static WalletStatsDto fromWalletTransactionStats(TrackWalletTransactionsUseCase.WalletTransactionStats stats) {
        return new WalletStatsDto(
            0, // Not available in this context
            0, // Not available in this context
            0, // Not available in this context
            0, // Not available in this context
            0, // Not available in this context
            0  // Not available in this context
        );
    }

    /**
     * Convert domain wallet stats to DTO
     */
    public static WalletStatsDto fromDomain(WalletAddressRepository.WalletStats stats) {
        return new WalletStatsDto(
            stats.getTotalWallets(),
            stats.getActiveWallets(),
            stats.getHighPriorityWallets(),
            stats.getExchangeWallets(),
            stats.getDefiWallets(),
            stats.getWhaleWallets()
        );
    }
}