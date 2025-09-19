package dev.bloco.wallet.bloom;

import org.web3j.crypto.Hash;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Simplified port of Hyperledger Besu's {@code LogsBloomFilter} tailored for tracking
 * blockchain events. It keeps the same bit layout and hashing algorithm so the
 * resulting bloom matches Besu's implementation.
 */
public class LogsBloomFilter {

    public static final int BYTE_SIZE = 256;

    private static final int LEAST_SIGNIFICANT_BYTE = 0xFF;
    private static final int LEAST_SIGNIFICANT_THREE_BITS = 0x7;
    private static final int BITS_IN_BYTE = 8;

    private final byte[] data;

    public LogsBloomFilter() {
        this(new byte[BYTE_SIZE]);
    }

    public LogsBloomFilter(byte[] data) {
        Objects.requireNonNull(data, "data");
        if (data.length != BYTE_SIZE) {
            throw new IllegalArgumentException(
                    "Invalid size for bloom filter backing array: expected " + BYTE_SIZE + " but got " + data.length);
        }
        this.data = data.clone();
    }

    public static LogsBloomFilter empty() {
        return new LogsBloomFilter();
    }

    public synchronized void insert(String value) {
        insert(value.getBytes(StandardCharsets.UTF_8));
    }

    public synchronized void insert(byte[] value) {
        Objects.requireNonNull(value, "value");
        byte[] hash = Hash.sha3(value);
        setBits(hash);
    }

    public synchronized void insertFilter(LogsBloomFilter other) {
        Objects.requireNonNull(other, "other");
        for (int i = 0; i < data.length; ++i) {
            data[i] = (byte) ((data[i] | other.data[i]) & 0xFF);
        }
    }

    public synchronized boolean mightContain(String value) {
        return mightContain(value.getBytes(StandardCharsets.UTF_8));
    }

    public synchronized boolean mightContain(byte[] value) {
        Objects.requireNonNull(value, "value");
        byte[] hash = Hash.sha3(value);
        return hasBits(hash);
    }

    public synchronized boolean couldContain(LogsBloomFilter subset) {
        if (subset == null) {
            return true;
        }
        if (subset.data.length != data.length) {
            return false;
        }
        for (int i = 0; i < data.length; ++i) {
            byte subsetValue = subset.data[i];
            if ((data[i] & subsetValue) != subsetValue) {
                return false;
            }
        }
        return true;
    }

    public synchronized byte[] toByteArray() {
        return data.clone();
    }

    private void setBits(byte[] hashValue) {
        for (int counter = 0; counter < 6; counter += 2) {
            int setBloomBit = ((hashValue[counter] & LEAST_SIGNIFICANT_THREE_BITS) << BITS_IN_BYTE)
                    + (hashValue[counter + 1] & LEAST_SIGNIFICANT_BYTE);
            setBit(setBloomBit);
        }
    }

    private boolean hasBits(byte[] hashValue) {
        for (int counter = 0; counter < 6; counter += 2) {
            int setBloomBit = ((hashValue[counter] & LEAST_SIGNIFICANT_THREE_BITS) << BITS_IN_BYTE)
                    + (hashValue[counter + 1] & LEAST_SIGNIFICANT_BYTE);
            if (!isBitSet(setBloomBit)) {
                return false;
            }
        }
        return true;
    }

    private void setBit(int index) {
        int byteIndex = BYTE_SIZE - 1 - index / 8;
        int bitIndex = index % 8;
        data[byteIndex] = (byte) (data[byteIndex] | (1 << bitIndex));
    }

    private boolean isBitSet(int index) {
        int byteIndex = BYTE_SIZE - 1 - index / 8;
        int bitIndex = index % 8;
        return (data[byteIndex] & (1 << bitIndex)) != 0;
    }

    @Override
    public synchronized boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LogsBloomFilter that)) {
            return false;
        }
        return Arrays.equals(data, that.data);
    }

    @Override
    public synchronized int hashCode() {
        return Arrays.hashCode(data);
    }
}
