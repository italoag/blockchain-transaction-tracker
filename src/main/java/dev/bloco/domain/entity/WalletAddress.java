package dev.bloco.domain.entity;

import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;

import java.time.Instant;
import java.util.Objects;

/**
 * Rich Domain Entity representing a wallet address to monitor.
 * Follows Rich Domain Model pattern with business logic encapsulated.
 */
public class WalletAddress {
    /**
     * Wallet types for categorization
     */
    public enum WalletType {
        EXCHANGE("Exchange Wallet"),
        DEFI("DeFi Protocol"),
        WHALE("High Value Wallet"),
        CONTRACT("Smart Contract"),
        PERSONAL("Personal Wallet"),
        UNKNOWN("Unknown");

        private final String displayName;

        WalletType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Priority levels for monitoring
     */
    public enum Priority {
        HIGH("High Priority - Real-time monitoring"),
        MEDIUM("Medium Priority - Regular monitoring"),
        LOW("Low Priority - Occasional monitoring");

        private final String description;

        Priority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private final Address address;
    private final Network network;
    private final Instant createdAt;
    private final String label;
    private final WalletType walletType;
    private final Priority priority;
    private boolean active;

    public WalletAddress(Address address, Network network, String label) {
        this(address, network, label, WalletType.UNKNOWN, Priority.MEDIUM);
    }

    public WalletAddress(Address address, Network network, String label, WalletType walletType, Priority priority) {
        this.address = address;
        this.network = network;
        this.label = label != null ? label : "";
        this.walletType = walletType != null ? walletType : WalletType.UNKNOWN;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.createdAt = Instant.now();
        this.active = true;
    }

    public WalletAddress(Address address, Network network) {
        this(address, network, null, WalletType.UNKNOWN, Priority.MEDIUM);
    }

    /**
     * Business method: Check if this wallet is involved in a transaction
     */
    public boolean isInvolvedIn(Transaction transaction) {
        return transaction.involvesAddress(address) && transaction.getNetwork().equals(network);
    }

    /**
     * Business method: Check if this wallet is the sender of a transaction
     */
    public boolean isSenderOf(Transaction transaction) {
        return transaction.isFrom(address) && transaction.getNetwork().equals(network);
    }

    /**
     * Business method: Check if this wallet is the receiver of a transaction
     */
    public boolean isReceiverOf(Transaction transaction) {
        return transaction.isTo(address) && transaction.getNetwork().equals(network);
    }

    /**
     * Business method: Deactivate monitoring for this wallet
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Business method: Reactivate monitoring for this wallet
     */
    public void reactivate() {
        this.active = true;
    }

    /**
     * Business method: Check if wallet has been monitored for more than specified days
     */
    public boolean hasBeenMonitoredForDays(long days) {
        long monitoredSeconds = Instant.now().getEpochSecond() - createdAt.getEpochSecond();
        long monitoredDays = monitoredSeconds / (24 * 60 * 60);
        return monitoredDays >= days;
    }

    /**
     * Business method: Get display name for the wallet
     */
    public String getDisplayName() {
        if (!label.isEmpty()) {
            return label;
        }
        return address.toString();
    }

    /**
     * Business method: Check if this is a contract address
     */
    public boolean isContractAddress() {
        return address.isContractAddress();
    }

    // Getters
    public Address getAddress() { return address; }
    public Network getNetwork() { return network; }
    public Instant getCreatedAt() { return createdAt; }
    public String getLabel() { return label; }
    public WalletType getWalletType() { return walletType; }
    public Priority getPriority() { return priority; }
    public boolean isActive() { return active; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WalletAddress that = (WalletAddress) o;
        return address.equals(that.address) && network.equals(that.network);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, network);
    }

    @Override
    public String toString() {
        return String.format("WalletAddress{address=%s, network=%s, active=%s}",
                           address, network, active);
    }
}