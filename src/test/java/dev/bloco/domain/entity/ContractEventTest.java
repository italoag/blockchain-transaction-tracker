package dev.bloco.domain.entity;

import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import dev.bloco.domain.valueobject.TransactionHash;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ContractEvent domain entity.
 * Tests business logic and domain rules.
 */
class ContractEventTest {

    private final Network ethereum = Network.of("ethereum", "Ethereum Mainnet", 1);
    private final TransactionHash transactionHash = TransactionHash.of("0x9fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8b");
    private final Address contractAddress = Address.of("0x742d35Cc6634C0532925a3b844Bc454e4438f44e");
    private final String eventSignature = "Transfer(address,address,uint256)";
    private final List<String> topics = Arrays.asList(
        "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
        "0x000000000000000000000000742d35cc6634c0532925a3b844bc454e4438f44e",
        "0x0000000000000000000000008ba1f109551bd432803012645261713d3e8f6c41"
    );
    private final String data = "0x0000000000000000000000000000000000000000000000000de0b6b3a7640000";
    private final Instant timestamp = Instant.now();
    private final int logIndex = 0;

    @Test
    void shouldCreateContractEventWithValidData() {
        // When
        ContractEvent event = new ContractEvent(
            transactionHash, contractAddress, eventSignature, topics, data,
            timestamp, ethereum, logIndex, false
        );

        // Then
        assertEquals(transactionHash, event.getTransactionHash());
        assertEquals(contractAddress, event.getContractAddress());
        assertEquals(eventSignature, event.getEventSignature());
        assertEquals(topics, event.getTopics());
        assertEquals(data, event.getData());
        assertEquals(timestamp, event.getTimestamp());
        assertEquals(ethereum, event.getNetwork());
        assertEquals(logIndex, event.getLogIndex());
        assertFalse(event.isRemoved());
    }

    @Test
    void shouldCreateRemovedEvent() {
        // When
        ContractEvent event = new ContractEvent(
            transactionHash, contractAddress, eventSignature, topics, data,
            timestamp, ethereum, logIndex, true
        );

        // Then
        assertTrue(event.isRemoved());
    }

    @Test
    void shouldCheckIfEventIsRecent() {
        // Given
        Instant recentTimestamp = Instant.now().minusSeconds(1800); // 30 minutes ago
        Instant oldTimestamp = Instant.now().minusSeconds(7200); // 2 hours ago

        ContractEvent recentEvent = new ContractEvent(
            transactionHash, contractAddress, eventSignature, topics, data,
            recentTimestamp, ethereum, logIndex, false
        );

        ContractEvent oldEvent = new ContractEvent(
            transactionHash, contractAddress, eventSignature, topics, data,
            oldTimestamp, ethereum, logIndex, false
        );

        // When & Then
        assertTrue(recentEvent.isRecent());
        assertFalse(oldEvent.isRecent());
    }

    @Test
    void shouldGetPrimaryTopic() {
        // Given
        ContractEvent event = new ContractEvent(
            transactionHash, contractAddress, eventSignature, topics, data,
            timestamp, ethereum, logIndex, false
        );

        // When
        String primaryTopic = event.getPrimaryTopic();

        // Then
        assertEquals(topics.get(0), primaryTopic);
    }

    @Test
    void shouldReturnNullForPrimaryTopicWhenNoTopics() {
        // Given
        List<String> emptyTopics = Arrays.asList();
        ContractEvent event = new ContractEvent(
            transactionHash, contractAddress, eventSignature, emptyTopics, data,
            timestamp, ethereum, logIndex, false
        );

        // When
        String primaryTopic = event.getPrimaryTopic();

        // Then
        assertNull(primaryTopic);
    }

    @Test
    void shouldIdentifyTransferEvent() {
        // Given
        ContractEvent transferEvent = new ContractEvent(
            transactionHash, contractAddress, eventSignature, topics, data,
            timestamp, ethereum, logIndex, false
        );

        List<String> nonTransferTopics = Arrays.asList("0x1234567890abcdef");
        ContractEvent nonTransferEvent = new ContractEvent(
            transactionHash, contractAddress, "Approval(address,address,uint256)",
            nonTransferTopics, data, timestamp, ethereum, logIndex, false
        );

        // When & Then
        assertTrue(transferEvent.isTransferEvent());
        assertFalse(nonTransferEvent.isTransferEvent());
    }

    @Test
    void shouldExtractTransferInfo() {
        // Given
        ContractEvent transferEvent = new ContractEvent(
            transactionHash, contractAddress, eventSignature, topics, data,
            timestamp, ethereum, logIndex, false
        );

        // When
        ContractEvent.TransferInfo transferInfo = transferEvent.getTransferInfo();

        // Then
        assertNotNull(transferInfo);
        assertEquals("0x742d35cc6634c0532925a3b844bc454e4438f44e", transferInfo.getFrom().getValue());
        assertEquals("0x8ba1f109551bd432803012645261713d3e8f6c41", transferInfo.getTo().getValue());
        assertEquals(data, transferInfo.getValue());
    }

    @Test
    void shouldReturnNullTransferInfoForNonTransferEvent() {
        // Given
        List<String> nonTransferTopics = Arrays.asList("0x1234567890abcdef");
        ContractEvent nonTransferEvent = new ContractEvent(
            transactionHash, contractAddress, "Approval(address,address,uint256)",
            nonTransferTopics, data, timestamp, ethereum, logIndex, false
        );

        // When
        ContractEvent.TransferInfo transferInfo = nonTransferEvent.getTransferInfo();

        // Then
        assertNull(transferInfo);
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        // Given
        ContractEvent event1 = new ContractEvent(
            transactionHash, contractAddress, eventSignature, topics, data,
            timestamp, ethereum, logIndex, false
        );

        ContractEvent event2 = new ContractEvent(
            transactionHash, contractAddress, eventSignature, topics, data,
            timestamp, ethereum, logIndex, false
        );

        ContractEvent differentEvent = new ContractEvent(
            TransactionHash.of("0x3fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8d"),
            contractAddress, eventSignature, topics, data, timestamp, ethereum, logIndex, false
        );

        // When & Then
        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
        assertNotEquals(event1, differentEvent);
        assertNotEquals(event1.hashCode(), differentEvent.hashCode());
    }

    @Test
    void shouldImplementToString() {
        // Given
        ContractEvent event = new ContractEvent(
            transactionHash, contractAddress, eventSignature, topics, data,
            timestamp, ethereum, logIndex, false
        );

        // When
        String stringRepresentation = event.toString();

        // Then
        assertTrue(stringRepresentation.contains("ContractEvent"));
        assertTrue(stringRepresentation.contains(transactionHash.getValue().substring(0, 10)));
        assertTrue(stringRepresentation.contains(contractAddress.getValue().substring(0, 10)));
        assertTrue(stringRepresentation.contains(eventSignature));
        assertTrue(stringRepresentation.contains(ethereum.getName()));
    }

    @Test
    void shouldHandleEmptyTopicsList() {
        // Given
        List<String> emptyTopics = Arrays.asList();
        ContractEvent event = new ContractEvent(
            transactionHash, contractAddress, eventSignature, emptyTopics, data,
            timestamp, ethereum, logIndex, false
        );

        // When & Then
        assertEquals(0, event.getTopics().size());
        assertFalse(event.isTransferEvent());
        assertNull(event.getPrimaryTopic());
    }
}