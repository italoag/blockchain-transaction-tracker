package dev.bloco.wallet.service;

import dev.bloco.wallet.bloom.LogsBloomFilter;
import org.springframework.stereotype.Service;

@Service
public class BloomFilterService {

    private final LogsBloomFilter bloomFilter = LogsBloomFilter.empty();

    public synchronized boolean mightContain(String network, String address) {
        String key = network + address;
        return bloomFilter.mightContain(key);
    }

    public synchronized void add(String network, String address) {
        String key = network + address;
        bloomFilter.insert(key);
    }
}
