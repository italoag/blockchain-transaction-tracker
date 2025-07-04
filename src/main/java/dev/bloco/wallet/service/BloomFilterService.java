package dev.bloco.wallet.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class BloomFilterService {

    private final ConcurrentMap<String, Boolean> dummyFilter = new ConcurrentHashMap<>();

    public boolean mightContain(String network, String address) {
        return dummyFilter.containsKey(network + address);
    }

    public void add(String network, String address) {
        dummyFilter.put(network + address, Boolean.TRUE);
    }
}
