package dev.bloco.presentation.dto;

import dev.bloco.domain.entity.Transaction;

import java.math.BigInteger;
import java.time.Instant;

/**
 * DTO for Transaction entity.
 * Used for API responses.
 */
public record TransactionDto(
    String hash,
    String from,
    String to,
    String network,
    Instant timestamp,
    boolean confirmed,
    BigInteger value
) {

    /**
     * Convert domain entity to DTO
     */
    public static TransactionDto fromDomain(Transaction transaction) {
        return new TransactionDto(
            transaction.getHash().getValue(),
            transaction.getFrom().getValue(),
            transaction.getTo() != null ? transaction.getTo().getValue() : null,
            transaction.getNetwork().getName(),
            transaction.getTimestamp(),
            transaction.isConfirmed(),
            transaction.getValue().getWei()
        );
    }
}