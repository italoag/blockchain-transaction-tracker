package dev.bloco.domain.entity;

import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import dev.bloco.domain.valueobject.TransactionHash;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

/**
 * Domain Entity representing a blockchain block.
 * Encapsulates block data and provides business logic related to blocks.
 */
public class Block {
    private final BigInteger number;
    private final TransactionHash hash;
    private final TransactionHash parentHash;
    private final Address miner;
    private final Instant timestamp;
    private final Network network;
    private final List<TransactionHash> transactionHashes;
    private final BigInteger gasUsed;
    private final BigInteger gasLimit;
    private final boolean confirmed;

    public Block(BigInteger number, TransactionHash hash, TransactionHash parentHash,
                 Address miner, Instant timestamp, Network network,
                 List<TransactionHash> transactionHashes, BigInteger gasUsed,
                 BigInteger gasLimit, boolean confirmed) {
        this.number = number;
        this.hash = hash;
        this.parentHash = parentHash;
        this.miner = miner;
        this.timestamp = timestamp;
        this.network = network;
        this.transactionHashes = List.copyOf(transactionHashes);
        this.gasUsed = gasUsed;
        this.gasLimit = gasLimit;
        this.confirmed = confirmed;
    }

    public BigInteger getNumber() { return number; }
    public TransactionHash getHash() { return hash; }
    public TransactionHash getParentHash() { return parentHash; }
    public Address getMiner() { return miner; }
    public Instant getTimestamp() { return timestamp; }
    public Network getNetwork() { return network; }
    public List<TransactionHash> getTransactionHashes() { return transactionHashes; }
    public BigInteger getGasUsed() { return gasUsed; }
    public BigInteger getGasLimit() { return gasLimit; }
    public boolean isConfirmed() { return confirmed; }

    /**
     * Check if block is recent (within last 10 minutes)
     */
    public boolean isRecent() {
        return Instant.now().minusSeconds(600).isBefore(timestamp);
    }

    /**
     * Calculate gas utilization percentage
     */
    public double getGasUtilizationPercentage() {
        if (gasLimit.equals(BigInteger.ZERO)) return 0.0;
        return gasUsed.doubleValue() / gasLimit.doubleValue() * 100.0;
    }

    /**
     * Check if block has high gas utilization (>90%)
     */
    public boolean hasHighGasUtilization() {
        return getGasUtilizationPercentage() > 90.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return hash.equals(block.hash) && network.equals(block.network);
    }

    @Override
    public int hashCode() {
        return hash.hashCode() * 31 + network.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Block{number=%s, hash=%s, network=%s, txCount=%d}",
                           number, hash.getValue(), network.getName(), transactionHashes.size());
    }
}