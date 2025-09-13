package dev.bloco.domain.entity;

import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import dev.bloco.domain.valueobject.TransactionHash;
import dev.bloco.domain.valueobject.TransactionValue;

import java.math.BigInteger;
import java.time.Instant;

/**
 * Rich Domain Entity representing a blockchain transaction.
 * Follows Rich Domain Model pattern with business logic encapsulated.
 */
public class Transaction {
    private final TransactionHash hash;
    private final Address from;
    private final Address to;
    private final Network network;
    private final TransactionValue value;
    private final BigInteger gasPrice;
    private final BigInteger gasLimit;
    private final BigInteger gasUsed;
    private final BigInteger blockNumber;
    private final String blockHash;
    private final int transactionIndex;
    private final int status;
    private final Instant timestamp;
    private final boolean confirmed;

    // Business state
    private TransactionStatus transactionStatus;

    public Transaction(TransactionHash hash, Address from, Address to, Network network,
                      TransactionValue value, BigInteger gasPrice, BigInteger gasLimit,
                      BigInteger gasUsed, BigInteger blockNumber, String blockHash,
                      int transactionIndex, int status, Instant timestamp, boolean confirmed) {
        this.hash = hash;
        this.from = from;
        this.to = to;
        this.network = network;
        this.value = value;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.gasUsed = gasUsed;
        this.blockNumber = blockNumber;
        this.blockHash = blockHash;
        this.transactionIndex = transactionIndex;
        this.status = status;
        this.timestamp = timestamp;
        this.confirmed = confirmed;
        this.transactionStatus = determineStatus();
    }

    /**
     * Business method: Check if transaction involves a specific address
     */
    public boolean involvesAddress(Address address) {
        return from.equals(address) || (to != null && to.equals(address));
    }

    /**
     * Business method: Check if transaction is from a specific address
     */
    public boolean isFrom(Address address) {
        return from.equals(address);
    }

    /**
     * Business method: Check if transaction is to a specific address
     */
    public boolean isTo(Address address) {
        return to != null && to.equals(address);
    }

    /**
     * Business method: Calculate transaction fee
     */
    public TransactionValue calculateFee() {
        if (gasUsed == null || gasPrice == null) {
            return TransactionValue.zero();
        }
        return TransactionValue.of(gasUsed.multiply(gasPrice));
    }

    /**
     * Business method: Check if transaction is successful
     */
    public boolean isSuccessful() {
        return status == 1;
    }

    /**
     * Business method: Check if transaction is pending
     */
    public boolean isPending() {
        return !confirmed;
    }

    /**
     * Business method: Check if transaction is confirmed
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Business method: Get transaction age in seconds
     */
    public long getAgeInSeconds() {
        return Instant.now().getEpochSecond() - timestamp.getEpochSecond();
    }

    /**
     * Business method: Determine transaction status based on current state
     */
    private TransactionStatus determineStatus() {
        if (!confirmed) {
            return TransactionStatus.PENDING;
        }
        return status == 1 ? TransactionStatus.SUCCESS : TransactionStatus.FAILED;
    }

    // Getters
    public TransactionHash getHash() { return hash; }
    public Address getFrom() { return from; }
    public Address getTo() { return to; }
    public Network getNetwork() { return network; }
    public TransactionValue getValue() { return value; }
    public BigInteger getGasPrice() { return gasPrice; }
    public BigInteger getGasLimit() { return gasLimit; }
    public BigInteger getGasUsed() { return gasUsed; }
    public BigInteger getBlockNumber() { return blockNumber; }
    public String getBlockHash() { return blockHash; }
    public int getTransactionIndex() { return transactionIndex; }
    public int getStatus() { return status; }
    public Instant getTimestamp() { return timestamp; }
    public TransactionStatus getTransactionStatus() { return transactionStatus; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return hash.equals(that.hash) && network.equals(that.network);
    }

    @Override
    public int hashCode() {
        return hash.hashCode() * 31 + network.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Transaction{hash=%s, from=%s, to=%s, network=%s, value=%s, status=%s}",
                           hash, from, to, network, value, transactionStatus);
    }

    public enum TransactionStatus {
        PENDING, SUCCESS, FAILED
    }
}