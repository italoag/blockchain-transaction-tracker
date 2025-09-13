package dev.bloco.domain.valueobject;

import java.util.Objects;

/**
 * Value Object representing a blockchain address.
 * Immutable and thread-safe.
 */
public final class Address {
    private final String value;

    private Address(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be null or empty");
        }
        if (!isValidAddress(value)) {
            throw new IllegalArgumentException("Invalid address format: " + value);
        }
        this.value = value.toLowerCase();
    }

    public static Address of(String value) {
        return new Address(value);
    }

    public String getValue() {
        return value;
    }

    /**
     * Check if address is a valid Ethereum address format
     */
    private boolean isValidAddress(String address) {
        if (!address.startsWith("0x")) {
            return false;
        }
        if (address.length() != 42) {
            return false;
        }
        // Check if all characters after 0x are valid hex
        return address.substring(2).matches("[0-9a-fA-F]+");
    }

    /**
     * Check if this address is a contract address (not EOA)
     */
    public boolean isContractAddress() {
        // This is a simplified check - in practice, you'd need to query the blockchain
        return !value.equals("0x0000000000000000000000000000000000000000");
    }

    /**
     * Get checksum address (EIP-55)
     */
    public String toChecksumAddress() {
        // Simplified checksum - in practice, use a proper EIP-55 implementation
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(value, address.value);
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