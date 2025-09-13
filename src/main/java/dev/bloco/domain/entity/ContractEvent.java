package dev.bloco.domain.entity;

import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import dev.bloco.domain.valueobject.TransactionHash;

import java.time.Instant;
import java.util.List;

/**
 * Domain Entity representing a smart contract event.
 * Encapsulates event data and provides business logic for event processing.
 */
public class ContractEvent {
    private final TransactionHash transactionHash;
    private final Address contractAddress;
    private final String eventSignature;
    private final List<String> topics;
    private final String data;
    private final Instant timestamp;
    private final Network network;
    private final int logIndex;
    private final boolean removed;

    public ContractEvent(TransactionHash transactionHash, Address contractAddress,
                        String eventSignature, List<String> topics, String data,
                        Instant timestamp, Network network, int logIndex, boolean removed) {
        this.transactionHash = transactionHash;
        this.contractAddress = contractAddress;
        this.eventSignature = eventSignature;
        this.topics = List.copyOf(topics);
        this.data = data;
        this.timestamp = timestamp;
        this.network = network;
        this.logIndex = logIndex;
        this.removed = removed;
    }

    public TransactionHash getTransactionHash() { return transactionHash; }
    public Address getContractAddress() { return contractAddress; }
    public String getEventSignature() { return eventSignature; }
    public List<String> getTopics() { return topics; }
    public String getData() { return data; }
    public Instant getTimestamp() { return timestamp; }
    public Network getNetwork() { return network; }
    public int getLogIndex() { return logIndex; }
    public boolean isRemoved() { return removed; }

    /**
     * Check if event is recent (within last hour)
     */
    public boolean isRecent() {
        return Instant.now().minusSeconds(3600).isBefore(timestamp);
    }

    /**
     * Get the first topic (usually the event signature hash)
     */
    public String getPrimaryTopic() {
        return topics.isEmpty() ? null : topics.get(0);
    }

    /**
     * Check if this is a transfer event (common ERC-20/ERC-721 pattern)
     */
    public boolean isTransferEvent() {
        if (topics.size() < 3) return false;
        String transferSignature = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";
        return topics.get(0).equals(transferSignature);
    }

    /**
     * Extract transfer information if this is a transfer event
     */
    public TransferInfo getTransferInfo() {
        if (!isTransferEvent() || topics.size() < 3) return null;

        return new TransferInfo(
            Address.of("0x" + topics.get(1).substring(26)), // from address (remove padding)
            Address.of("0x" + topics.get(2).substring(26)), // to address (remove padding)
            data // amount/value
        );
    }

    /**
     * Transfer information extracted from event
     */
    public static class TransferInfo {
        private final Address from;
        private final Address to;
        private final String value;

        public TransferInfo(Address from, Address to, String value) {
            this.from = from;
            this.to = to;
            this.value = value;
        }

        public Address getFrom() { return from; }
        public Address getTo() { return to; }
        public String getValue() { return value; }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContractEvent that = (ContractEvent) o;
        return transactionHash.equals(that.transactionHash) &&
               contractAddress.equals(that.contractAddress) &&
               logIndex == that.logIndex &&
               network.equals(that.network);
    }

    @Override
    public int hashCode() {
        return transactionHash.hashCode() * 31 +
               contractAddress.hashCode() * 31 +
               logIndex * 31 +
               network.hashCode();
    }

    @Override
    public String toString() {
        return String.format("ContractEvent{tx=%s, contract=%s, event=%s, network=%s}",
                           transactionHash.getValue(),
                           contractAddress.getValue(),
                           eventSignature,
                           network.getName());
    }
}