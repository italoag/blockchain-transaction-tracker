package dev.bloco.wallet.bloom;

import org.junit.jupiter.api.Test;
import org.web3j.crypto.Hash;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpString;
import org.web3j.utils.Numeric;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogsBloomFilterTest {

    @Test
    void builderInsertMatchesBesuBitLayout() {
        LogsBloomFilter filter = LogsBloomFilter.builder()
                .insertBytes("network-address".getBytes(StandardCharsets.UTF_8))
                .build();

        byte[] bloomBytes = filter.toByteArray();
        int[] expectedBits = bloomBits("network-address");
        for (int bit : expectedBits) {
            assertTrue(isBitSet(bloomBytes, bit), "expected bit not set: " + bit);
        }
    }

    @Test
    void directInsertMatchesBuilder() {
        byte[] value = "network-address".getBytes(StandardCharsets.UTF_8);

        LogsBloomFilter viaBuilder = LogsBloomFilter.builder()
                .insertBytes(value)
                .build();

        LogsBloomFilter viaInsert = LogsBloomFilter.empty();
        viaInsert.insert(value);

        assertEquals(viaBuilder, viaInsert);
    }

    @Test
    void builderSupportsLogsAndTopics() {
        LogsBloomFilter.LogTopic topicOne = LogsBloomFilter.LogTopic.fromHexString("0x01");
        LogsBloomFilter.LogTopic topicTwo = LogsBloomFilter.LogTopic.fromHexString("0x02");
        LogsBloomFilter.Log log = new LogsBloomFilter.Log(
                Numeric.hexStringToByteArray("0x0F572E5295C57F15886F9B263E2F6D2D6C7B5EC6"),
                new byte[0],
                List.of(topicOne, topicTwo));

        LogsBloomFilter filter = LogsBloomFilter.builder()
                .insertLog(log)
                .build();

        assertTrue(filter.couldContain(LogsBloomFilter.builder().insertLog(log).build()));
    }

    @Test
    void mergeFiltersKeepsBits() {
        LogsBloomFilter first = LogsBloomFilter.builder()
                .insertBytes("value-one".getBytes(StandardCharsets.UTF_8))
                .build();
        LogsBloomFilter second = LogsBloomFilter.builder()
                .insertBytes("value-two".getBytes(StandardCharsets.UTF_8))
                .build();

        LogsBloomFilter merged = LogsBloomFilter.builder()
                .insertFilter(first)
                .insertFilter(second)
                .build();

        assertTrue(merged.couldContain(first));
        assertTrue(merged.couldContain(second));
    }

    @Test
    void readFromRlpRestoresBloom() {
        LogsBloomFilter original = LogsBloomFilter.builder()
                .insertBytes("rlp-input".getBytes(StandardCharsets.UTF_8))
                .build();

        byte[] encoded = RlpEncoder.encode(RlpString.create(original.toByteArray()));
        LogsBloomFilter decoded = LogsBloomFilter.readFrom(encoded);

        assertEquals(original, decoded);
    }

    @Test
    void hexConstructionProducesSameBytes() {
        LogsBloomFilter filter = LogsBloomFilter.builder()
                .insertBytes("hex".getBytes(StandardCharsets.UTF_8))
                .build();
        LogsBloomFilter fromHex = LogsBloomFilter.fromHexString(filter.toHexString());

        assertArrayEquals(filter.toByteArray(), fromHex.toByteArray());
    }

    @Test
    void mutableOperationsMaintainCompatibility() {
        LogsBloomFilter filter = LogsBloomFilter.empty();
        filter.insert("network-address");

        assertTrue(filter.mightContain("network-address"));
        assertFalse(filter.mightContain("other"));
    }

    @Test
    void clearResetsAllBits() {
        LogsBloomFilter filter = LogsBloomFilter.empty();
        filter.insert("value");
        assertTrue(filter.mightContain("value"));

        filter.clear();

        assertFalse(filter.mightContain("value"));
        assertEquals(new LogsBloomFilter(), filter);
    }

    private static int[] bloomBits(String value) {
        byte[] hash = Hash.sha3(value.getBytes(StandardCharsets.UTF_8));
        int[] bits = new int[3];
        int index = 0;
        for (int counter = 0; counter < 6; counter += 2) {
            bits[index++] = ((hash[counter] & 0x7) << 8) + (hash[counter + 1] & 0xFF);
        }
        return bits;
    }

    private static boolean isBitSet(byte[] data, int index) {
        int byteIndex = LogsBloomFilter.BYTE_SIZE - 1 - index / 8;
        int bitIndex = index % 8;
        return (data[byteIndex] & (1 << bitIndex)) != 0;
    }
}
