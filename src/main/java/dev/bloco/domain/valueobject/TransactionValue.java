package dev.bloco.domain.valueobject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object representing a transaction value in wei.
 * Immutable and thread-safe with conversion utilities.
 */
public final class TransactionValue implements Comparable<TransactionValue> {
    private final BigInteger wei;

    private TransactionValue(BigInteger wei) {
        if (wei == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        if (wei.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Value cannot be negative");
        }
        this.wei = wei;
    }

    public static TransactionValue of(BigInteger wei) {
        return new TransactionValue(wei);
    }

    public static TransactionValue of(String wei) {
        return new TransactionValue(new BigInteger(wei));
    }

    public static TransactionValue of(long wei) {
        return new TransactionValue(BigInteger.valueOf(wei));
    }

    public static TransactionValue zero() {
        return new TransactionValue(BigInteger.ZERO);
    }

    public BigInteger getWei() {
        return wei;
    }

    /**
     * Convert wei to ether
     */
    public BigDecimal toEther() {
        return new BigDecimal(wei).divide(BigDecimal.valueOf(1_000_000_000_000_000_000L), 18, RoundingMode.HALF_UP);
    }

    /**
     * Convert wei to gwei
     */
    public BigDecimal toGwei() {
        return new BigDecimal(wei).divide(BigDecimal.valueOf(1_000_000_000L), 9, RoundingMode.HALF_UP);
    }

    /**
     * Check if value is zero
     */
    public boolean isZero() {
        return wei.equals(BigInteger.ZERO);
    }

    /**
     * Check if value is positive
     */
    public boolean isPositive() {
        return wei.compareTo(BigInteger.ZERO) > 0;
    }

    /**
     * Add two transaction values
     */
    public TransactionValue add(TransactionValue other) {
        return new TransactionValue(this.wei.add(other.wei));
    }

    /**
     * Subtract transaction values
     */
    public TransactionValue subtract(TransactionValue other) {
        BigInteger result = this.wei.subtract(other.wei);
        if (result.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Subtraction would result in negative value");
        }
        return new TransactionValue(result);
    }

    /**
     * Multiply by scalar
     */
    public TransactionValue multiply(long scalar) {
        if (scalar < 0) {
            throw new IllegalArgumentException("Scalar cannot be negative");
        }
        return new TransactionValue(this.wei.multiply(BigInteger.valueOf(scalar)));
    }

    /**
     * Get formatted string representation
     */
    public String toFormattedString() {
        if (isZero()) {
            return "0 ETH";
        }

        BigDecimal ether = toEther();
        if (ether.compareTo(BigDecimal.ONE) >= 0) {
            return ether.stripTrailingZeros().toPlainString() + " ETH";
        } else {
            return toGwei().stripTrailingZeros().toPlainString() + " Gwei";
        }
    }

    @Override
    public int compareTo(TransactionValue other) {
        return this.wei.compareTo(other.wei);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionValue that = (TransactionValue) o;
        return Objects.equals(wei, that.wei);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wei);
    }

    @Override
    public String toString() {
        return wei.toString() + " wei";
    }
}