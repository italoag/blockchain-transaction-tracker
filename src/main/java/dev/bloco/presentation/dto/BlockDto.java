package dev.bloco.presentation.dto;

import dev.bloco.domain.entity.Block;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

/**
 * DTO for Block entity.
 * Used for API responses.
 */
public record BlockDto(
    BigInteger number,
    String hash,
    String parentHash,
    String miner,
    Instant timestamp,
    String network,
    List<String> transactionHashes,
    BigInteger gasUsed,
    BigInteger gasLimit,
    boolean confirmed
) {

    /**
     * Convert domain entity to DTO
     */
    public static BlockDto fromDomain(Block block) {
        return new BlockDto(
            block.getNumber(),
            block.getHash().getValue(),
            block.getParentHash().getValue(),
            block.getMiner().getValue(),
            block.getTimestamp(),
            block.getNetwork().getName(),
            block.getTransactionHashes().stream()
                .map(hash -> hash.getValue())
                .toList(),
            block.getGasUsed(),
            block.getGasLimit(),
            block.isConfirmed()
        );
    }
}