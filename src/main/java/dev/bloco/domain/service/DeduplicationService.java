package dev.bloco.domain.service;

/**
 * Domain Service interface for transaction deduplication operations.
 * Defines the contract for bloom filter operations following DDD principles.
 */
public interface DeduplicationService {

    /**
     * Check if a transaction hash has been seen before on a network
     */
    boolean mightContain(String network, String transactionHash);

    /**
     * Mark a transaction hash as seen on a network
     */
    void add(String network, String transactionHash);

    /**
     * Clear all deduplication data for a network
     */
    void clear(String network);

    /**
     * Get approximate count of items in the filter for a network
     */
    long approximateCount(String network);

    /**
     * Get false positive probability for a network
     */
    double falsePositiveProbability(String network);
}