package dev.bloco.wallet.bloom;

import org.junit.jupiter.api.Test;

import org.web3j.crypto.Hash;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogsBloomFilterTest {

    @Test
    void insertAndContain() {
        LogsBloomFilter filter = LogsBloomFilter.empty();
        filter.insert("network-address");

        assertTrue(filter.mightContain("network-address"));

        byte[] bloomBytes = filter.toByteArray();
        int[] expectedBits = bloomBits("network-address");
        for (int bit : expectedBits) {
            assertTrue(isBitSet(bloomBytes, bit), "expected bit not set: " + bit);
        }

        int[] otherBits = bloomBits("other");
        boolean overlapsAll = true;
        for (int bit : otherBits) {
            if (!isBitSet(bloomBytes, bit)) {
                overlapsAll = false;
                break;
            }
        }
        assertFalse(overlapsAll, "different value should not match all bloom bits");
    }

    @Test
    void mergeFilters() {
        LogsBloomFilter primary = LogsBloomFilter.empty();
        primary.insert("value-one");

        LogsBloomFilter secondary = LogsBloomFilter.empty();
        secondary.insert("value-two");

        primary.insertFilter(secondary);

        assertTrue(primary.mightContain("value-one"));
        assertTrue(primary.mightContain("value-two"));
    }

    @Test
    void couldContainSubset() {
        LogsBloomFilter base = LogsBloomFilter.empty();
        base.insert("value-one");

        LogsBloomFilter same = LogsBloomFilter.empty();
        same.insert("value-one");

        LogsBloomFilter different = LogsBloomFilter.empty();
        different.insert("value-two");

        assertTrue(base.couldContain(same));
        assertFalse(base.couldContain(different));
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
