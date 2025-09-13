package dev.bloco.domain.valueobject;

import java.util.Objects;

/**
 * Value Object representing a transaction hash.
 * Immutable and thread-safe.
 */
public final class TransactionHash {
    private final String value;

    private TransactionHash(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction hash cannot be null or empty");
        }
        if (!isValidHash(value)) {
            throw new IllegalArgumentException("Invalid transaction hash format: " + value);
        }
        this.value = value.toLowerCase();
    }

    public static TransactionHash of(String value) {
        return new TransactionHash(value);
    }

    public String getValue() {
        return value;
    }

    /**
     * Check if hash is a valid Ethereum transaction hash format
     */
    private boolean isValidHash(String hash) {
        if (!hash.startsWith("0x")) {
            return false;
        }
        if (hash.length() != 66) { // 0x + 64 hex characters
            return false;
        }
        // Check if all characters after 0x are valid hex
        return hash.substring(2).matches("[0-9a-fA-F]+");
    }

    /**
     * Get shortened version for display (first 6 + last 7 characters)
     */
    public String toShortString() {
        if (value.length() <= 13) {
            return value;
        }
        String first = value.substring(0, 8); // 0x + 6 chars
        String last = value.substring(value.length() - 7); // last 7 chars
        return first + "..." + last;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionHash that = (TransactionHash) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}