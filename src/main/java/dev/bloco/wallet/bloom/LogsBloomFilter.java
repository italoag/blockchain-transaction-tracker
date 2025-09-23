package dev.bloco.wallet.bloom;

import org.web3j.crypto.Hash;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Port of Hyperledger Besu's {@code LogsBloomFilter}. The implementation mirrors the
 * hashing and bit layout rules used by Besu so bloom filters generated here remain
 * interoperable with Besu-based blockchain networks.
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

    public LogsBloomFilter(String logsBloomHexString) {
        this(Numeric.hexStringToByteArray(Objects.requireNonNull(logsBloomHexString, "logsBloomHexString")));
    }

    public static LogsBloomFilter fromHexString(String hexString) {
        return new LogsBloomFilter(hexString);
    }

    public static LogsBloomFilter empty() {
        return new LogsBloomFilter();
    }

    public static LogsBloomFilter readFrom(byte[] rlpEncodedBloom) {
        Objects.requireNonNull(rlpEncodedBloom, "rlpEncodedBloom");
        final RlpList decoded = RlpDecoder.decode(rlpEncodedBloom);
        if (decoded.getValues().isEmpty()) {
            throw new IllegalArgumentException("LogsBloomFilter RLP input cannot be empty");
        }
        final RlpType value = decoded.getValues().getFirst();
        if (!(value instanceof RlpString rlpString)) {
            throw new IllegalArgumentException("LogsBloomFilter RLP input must be a byte string");
        }
        final byte[] bloomBytes = rlpString.getBytes();
        if (bloomBytes.length != BYTE_SIZE) {
            throw new IllegalArgumentException(
                    "LogsBloomFilter unexpected size of " + bloomBytes.length + " (needs " + BYTE_SIZE + ")");
        }
        return new LogsBloomFilter(bloomBytes);
    }

    public synchronized boolean couldContain(LogsBloomFilter subset) {
        if (subset == null) {
            return true;
        }
        if (subset.size() != size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            byte subsetValue = subset.get(i);
            if ((get(i) & subsetValue) != subsetValue) {
                return false;
            }
        }
        return true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int size() {
        return data.length;
    }

    public byte get(int index) {
        return data[index];
    }

    public synchronized void insert(String value) {
        insert(value.getBytes(StandardCharsets.UTF_8));
    }

    public synchronized void insert(byte[] value) {
        Objects.requireNonNull(value, "value");
        LogsBloomFilter updated = builder().insertFilter(this).insertBytes(value).build();
        replaceWith(updated);
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
        LogsBloomFilter candidate = builder().insertBytes(value).build();
        return couldContain(candidate);
    }

    public synchronized byte[] toByteArray() {
        return data.clone();
    }

    public synchronized String toHexString() {
        return Numeric.toHexString(data);
    }

    private void replaceWith(LogsBloomFilter other) {
        System.arraycopy(other.data, 0, data, 0, data.length);
    }

    @Override
    public synchronized boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LogsBloomFilter that)) {
            return false;
        }
        for (int i = 0; i < data.length; i++) {
            if (data[i] != that.data[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public synchronized int hashCode() {
        int result = 1;
        for (byte datum : data) {
            result = 31 * result + datum;
        }
        return result;
    }

    public static final class Builder {

        private final byte[] data;

        private Builder() {
            this.data = new byte[LogsBloomFilter.BYTE_SIZE];
        }

        public Builder insertFilter(LogsBloomFilter other) {
            Objects.requireNonNull(other, "other");
            for (int i = 0; i < data.length; ++i) {
                data[i] = (byte) ((data[i] | other.data[i]) & 0xFF);
            }
            return this;
        }

        public Builder insertLog(Log log) {
            Objects.requireNonNull(log, "log");
            insertBytes(log.getLogger());
            for (LogTopic topic : log.getTopics()) {
                insertBytes(topic.getBytes());
            }
            return this;
        }

        public Builder insertLogs(Collection<Log> logs) {
            Objects.requireNonNull(logs, "logs");
            logs.forEach(this::insertLog);
            return this;
        }

        public Builder insertBytes(byte[] value) {
            Objects.requireNonNull(value, "value");
            setBits(Hash.sha3(value));
            return this;
        }

        public LogsBloomFilter build() {
            return new LogsBloomFilter(data);
        }

        private void setBits(byte[] hashValue) {
            for (int counter = 0; counter < 6; counter += 2) {
                int setBloomBit = ((hashValue[counter] & LEAST_SIGNIFICANT_THREE_BITS) << BITS_IN_BYTE)
                        + (hashValue[counter + 1] & LEAST_SIGNIFICANT_BYTE);
                setBit(setBloomBit);
            }
        }

        private void setBit(int index) {
            int byteIndex = BYTE_SIZE - 1 - index / 8;
            int bitIndex = index % 8;
            data[byteIndex] = (byte) (data[byteIndex] | (1 << bitIndex));
        }
    }

    public static final class Log {
        private final byte[] logger;
        private final byte[] data;
        private final List<LogTopic> topics;

        public Log(byte[] logger, byte[] data, List<LogTopic> topics) {
            this.logger = Objects.requireNonNull(logger, "logger").clone();
            this.data = data == null ? new byte[0] : data.clone();
            this.topics = List.copyOf(Objects.requireNonNull(topics, "topics"));
        }

        public byte[] getLogger() {
            return logger.clone();
        }

        public byte[] getData() {
            return data.clone();
        }

        public List<LogTopic> getTopics() {
            return topics;
        }
    }

    public static final class LogTopic {
        private final byte[] data;

        public LogTopic(byte[] data) {
            this.data = Objects.requireNonNull(data, "data").clone();
        }

        public static LogTopic fromHexString(String hex) {
            return new LogTopic(Numeric.hexStringToByteArray(Objects.requireNonNull(hex, "hex")));
        }

        public byte[] getBytes() {
            return data.clone();
        }
    }
}
