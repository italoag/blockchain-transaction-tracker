package dev.bloco.domain.entity;

import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import dev.bloco.domain.valueobject.TransactionHash;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Block domain entity.
 * Tests business logic and domain rules.
 */
class BlockTest {

    private final Network ethereum = Network.of("ethereum", "Ethereum Mainnet", 1);
    private final BigInteger blockNumber = BigInteger.valueOf(18500000);
    private final TransactionHash blockHash = TransactionHash.of("0x9fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8b");
    private final TransactionHash parentHash = TransactionHash.of("0x8fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8a");
    private final Address miner = Address.of("0x742d35Cc6634C0532925a3b844Bc454e4438f44e");
    private final Instant timestamp = Instant.now();
    private final List<TransactionHash> transactionHashes = Arrays.asList(
        TransactionHash.of("0x1fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8b"),
        TransactionHash.of("0x2fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8c")
    );
    private final BigInteger gasUsed = BigInteger.valueOf(15000000);
    private final BigInteger gasLimit = BigInteger.valueOf(30000000);

    @Test
    void shouldCreateBlockWithValidData() {
        // When
        Block block = new Block(
            blockNumber, blockHash, parentHash, miner, timestamp, ethereum,
            transactionHashes, gasUsed, gasLimit, true
        );

        // Then
        assertEquals(blockNumber, block.getNumber());
        assertEquals(blockHash, block.getHash());
        assertEquals(parentHash, block.getParentHash());
        assertEquals(miner, block.getMiner());
        assertEquals(timestamp, block.getTimestamp());
        assertEquals(ethereum, block.getNetwork());
        assertEquals(transactionHashes, block.getTransactionHashes());
        assertEquals(gasUsed, block.getGasUsed());
        assertEquals(gasLimit, block.getGasLimit());
        assertTrue(block.isConfirmed());
    }

    @Test
    void shouldCreateUnconfirmedBlock() {
        // When
        Block block = new Block(
            blockNumber, blockHash, parentHash, miner, timestamp, ethereum,
            transactionHashes, gasUsed, gasLimit, false
        );

        // Then
        assertFalse(block.isConfirmed());
    }

    @Test
    void shouldCalculateGasUtilizationPercentage() {
        // Given
        Block block = new Block(
            blockNumber, blockHash, parentHash, miner, timestamp, ethereum,
            transactionHashes, gasUsed, gasLimit, true
        );

        // When
        double utilization = block.getGasUtilizationPercentage();

        // Then
        assertEquals(50.0, utilization, 0.01);
    }

    @Test
    void shouldHandleZeroGasLimit() {
        // Given
        Block block = new Block(
            blockNumber, blockHash, parentHash, miner, timestamp, ethereum,
            transactionHashes, gasUsed, BigInteger.ZERO, true
        );

        // When
        double utilization = block.getGasUtilizationPercentage();

        // Then
        assertEquals(0.0, utilization, 0.01);
    }

    @Test
    void shouldCheckHighGasUtilization() {
        // Given
        BigInteger highGasUsed = BigInteger.valueOf(28000000); // 93.3% utilization
        Block block = new Block(
            blockNumber, blockHash, parentHash, miner, timestamp, ethereum,
            transactionHashes, highGasUsed, gasLimit, true
        );

        // When & Then
        assertTrue(block.hasHighGasUtilization());
    }

    @Test
    void shouldCheckLowGasUtilization() {
        // Given
        BigInteger lowGasUsed = BigInteger.valueOf(5000000); // 16.7% utilization
        Block block = new Block(
            blockNumber, blockHash, parentHash, miner, timestamp, ethereum,
            transactionHashes, lowGasUsed, gasLimit, true
        );

        // When & Then
        assertFalse(block.hasHighGasUtilization());
    }

    @Test
    void shouldCheckIfBlockIsRecent() {
        // Given
        Instant recentTimestamp = Instant.now().minusSeconds(300); // 5 minutes ago
        Block recentBlock = new Block(
            blockNumber, blockHash, parentHash, miner, recentTimestamp, ethereum,
            transactionHashes, gasUsed, gasLimit, true
        );

        Instant oldTimestamp = Instant.now().minusSeconds(1200); // 20 minutes ago
        Block oldBlock = new Block(
            blockNumber, blockHash, parentHash, miner, oldTimestamp, ethereum,
            transactionHashes, gasUsed, gasLimit, true
        );

        // When & Then
        assertTrue(recentBlock.isRecent());
        assertFalse(oldBlock.isRecent());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        // Given
        Block block1 = new Block(
            blockNumber, blockHash, parentHash, miner, timestamp, ethereum,
            transactionHashes, gasUsed, gasLimit, true
        );
        Block block2 = new Block(
            BigInteger.valueOf(18500001), blockHash, parentHash, miner, timestamp, ethereum,
            transactionHashes, gasUsed, gasLimit, true
        );
        Block differentBlock = new Block(
            blockNumber, TransactionHash.of("0x3fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8d"),
            parentHash, miner, timestamp, ethereum, transactionHashes, gasUsed, gasLimit, true
        );

        // When & Then
        assertEquals(block1, block2); // Same hash and network
        assertEquals(block1.hashCode(), block2.hashCode());
        assertNotEquals(block1, differentBlock);
        assertNotEquals(block1.hashCode(), differentBlock.hashCode());
    }

    @Test
    void shouldImplementToString() {
        // Given
        Block block = new Block(
            blockNumber, blockHash, parentHash, miner, timestamp, ethereum,
            transactionHashes, gasUsed, gasLimit, true
        );

        // When
        String stringRepresentation = block.toString();

        // Then
        assertTrue(stringRepresentation.contains("Block"));
        assertTrue(stringRepresentation.contains(blockNumber.toString()));
        assertTrue(stringRepresentation.contains(blockHash.getValue().substring(0, 10)));
        assertTrue(stringRepresentation.contains(ethereum.getName()));
        assertTrue(stringRepresentation.contains("2")); // transaction count
    }

    @Test
    void shouldHandleEmptyTransactionList() {
        // Given
        List<TransactionHash> emptyTransactions = Arrays.asList();
        Block block = new Block(
            blockNumber, blockHash, parentHash, miner, timestamp, ethereum,
            emptyTransactions, gasUsed, gasLimit, true
        );

        // When & Then
        assertEquals(0, block.getTransactionHashes().size());
    }
}