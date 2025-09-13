package dev.bloco.domain.valueobject;

import java.util.Objects;

/**
 * Value Object representing a blockchain network.
 * Immutable and thread-safe.
 */
public final class Network {
    private final String id;
    private final String name;
    private final int chainId;

    private Network(String id, String name, int chainId) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Network ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Network name cannot be null or empty");
        }
        this.id = id.toLowerCase();
        this.name = name;
        this.chainId = chainId;
    }

    public static Network of(String id, String name, int chainId) {
        return new Network(id, name, chainId);
    }

    public static Network ethereum() {
        return new Network("ethereum", "Ethereum", 1);
    }

    public static Network polygon() {
        return new Network("polygon", "Polygon", 137);
    }

    public static Network bsc() {
        return new Network("bsc", "Binance Smart Chain", 56);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getChainId() {
        return chainId;
    }

    /**
     * Check if this is a mainnet network
     */
    public boolean isMainnet() {
        return chainId == 1 || chainId == 137 || chainId == 56;
    }

    /**
     * Check if this is a testnet network
     */
    public boolean isTestnet() {
        return !isMainnet();
    }

    /**
     * Get the native currency symbol for this network
     */
    public String getNativeCurrency() {
        return switch (chainId) {
            case 1 -> "ETH";
            case 137 -> "MATIC";
            case 56 -> "BNB";
            default -> "UNKNOWN";
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Network network = (Network) o;
        return chainId == network.chainId &&
               Objects.equals(id, network.id) &&
               Objects.equals(name, network.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, chainId);
    }

    @Override
    public String toString() {
        return String.format("%s Network (%s) - Chain ID: %d", name, id, chainId);
    }
}