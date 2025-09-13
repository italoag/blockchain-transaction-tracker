package dev.bloco.presentation.dto;

import dev.bloco.domain.entity.ContractEvent;

import java.time.Instant;
import java.util.List;

/**
 * DTO for ContractEvent entity.
 * Used for API responses.
 */
public record ContractEventDto(
    String transactionHash,
    String contractAddress,
    String eventSignature,
    List<String> topics,
    String data,
    Instant timestamp,
    String network,
    int logIndex,
    boolean removed
) {

    /**
     * Convert domain entity to DTO
     */
    public static ContractEventDto fromDomain(ContractEvent event) {
        return new ContractEventDto(
            event.getTransactionHash().getValue(),
            event.getContractAddress().getValue(),
            event.getEventSignature(),
            event.getTopics(),
            event.getData(),
            event.getTimestamp(),
            event.getNetwork().getName(),
            event.getLogIndex(),
            event.isRemoved()
        );
    }
}