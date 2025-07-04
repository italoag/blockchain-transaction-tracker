package dev.bloco.wallet.service;

import dev.bloco.wallet.util.BloomFilterUtil;
import org.springframework.stereotype.Service;

import java.util.BitSet;

@Service
public class BloomFilterService {

    private static final int DEFAULT_SIZE = 1 << 24; // 16M bits
    private static final int[] SEEDS = new int[] {0x7f3a21eb, 0x6b8f7b99, 0x2d3c9f5d};

    private final BitSet bitSet = new BitSet(DEFAULT_SIZE);

    private int indexFor(String key, int seed) {
        int hash = BloomFilterUtil.hash(key, seed);
        hash = Math.abs(hash);
        return hash % DEFAULT_SIZE;
    }

    public synchronized boolean mightContain(String network, String address) {
        String key = network + address;
        for (int seed : SEEDS) {
            if (!bitSet.get(indexFor(key, seed))) {
                return false;
            }
        }
        return true;
    }

    public synchronized void add(String network, String address) {
        String key = network + address;
        for (int seed : SEEDS) {
            bitSet.set(indexFor(key, seed));
        }
    }
}
