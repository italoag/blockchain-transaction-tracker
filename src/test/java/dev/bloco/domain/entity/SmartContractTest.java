package dev.bloco.domain.entity;

import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SmartContract domain entity.
 * Tests business logic and domain rules.
 */
class SmartContractTest {

    private final Network ethereum = Network.of("ethereum", "Ethereum Mainnet", 1);
    private final Address contractAddress = Address.of("0x742d35Cc6634C0532925a3b844Bc454e4438f44e");
    private final String contractName = "MyToken";
    private final String abi = "[{\"inputs\":[],\"name\":\"name\",\"outputs\":[],\"stateMutability\":\"view\",\"type\":\"function\"}]";
    private final List<String> eventsToMonitor = Arrays.asList(
        "Transfer(address,address,uint256)",
        "Approval(address,address,uint256)"
    );

    @Test
    void shouldCreateSmartContractWithValidData() {
        // When
        SmartContract contract = new SmartContract(
            contractAddress, ethereum, contractName, abi, eventsToMonitor
        );

        // Then
        assertEquals(contractAddress, contract.getAddress());
        assertEquals(ethereum, contract.getNetwork());
        assertEquals(contractName, contract.getName());
        assertEquals(abi, contract.getAbi());
        assertEquals(eventsToMonitor, contract.getEventsToMonitor());
        assertEquals(SmartContract.ContractType.UNKNOWN, contract.getContractType());
        assertTrue(contract.isActive());
        assertNotNull(contract.getDeployedAt());
    }

    @Test
    void shouldCreateSmartContractWithContractType() {
        // When
        SmartContract contract = new SmartContract(
            contractAddress, ethereum, contractName, abi, eventsToMonitor,
            SmartContract.ContractType.ERC20
        );

        // Then
        assertEquals(SmartContract.ContractType.ERC20, contract.getContractType());
    }

    @Test
    void shouldCheckIfContractEmitsEvent() {
        // Given
        SmartContract contract = new SmartContract(
            contractAddress, ethereum, contractName, abi, eventsToMonitor
        );

        // When & Then
        assertTrue(contract.emitsEvent("Transfer(address,address,uint256)"));
        assertTrue(contract.emitsEvent("Approval(address,address,uint256)"));
        assertFalse(contract.emitsEvent("Mint(address,uint256)"));
    }

    @Test
    void shouldCheckIfContractMonitorsAllEvents() {
        // Given
        SmartContract contractWithEvents = new SmartContract(
            contractAddress, ethereum, contractName, abi, eventsToMonitor
        );

        SmartContract contractWithoutEvents = new SmartContract(
            Address.of("0x8ba1f109551bD432803012645261713D3e8f6c41"),
            ethereum, "EmptyContract", abi, List.of()
        );

        // When & Then
        assertFalse(contractWithEvents.monitorsAllEvents());
        assertTrue(contractWithoutEvents.monitorsAllEvents());
    }

    @Test
    void shouldAddEventToMonitor() {
        // Given
        SmartContract contract = new SmartContract(
            contractAddress, ethereum, contractName, abi, eventsToMonitor
        );

        // When
        SmartContract updatedContract = contract.addEventToMonitor("Mint(address,uint256)");

        // Then
        assertTrue(updatedContract.emitsEvent("Mint(address,uint256)"));
        assertEquals(3, updatedContract.getEventsToMonitor().size());
        // Original contract should remain unchanged (immutable)
        assertFalse(contract.emitsEvent("Mint(address,uint256)"));
        assertEquals(2, contract.getEventsToMonitor().size());
    }

    @Test
    void shouldRemoveEventToMonitor() {
        // Given
        SmartContract contract = new SmartContract(
            contractAddress, ethereum, contractName, abi, eventsToMonitor
        );

        // When
        SmartContract updatedContract = contract.removeEventToMonitor("Transfer(address,address,uint256)");

        // Then
        assertFalse(updatedContract.emitsEvent("Transfer(address,address,uint256)"));
        assertTrue(updatedContract.emitsEvent("Approval(address,address,uint256)"));
        assertEquals(1, updatedContract.getEventsToMonitor().size());
        // Original contract should remain unchanged (immutable)
        assertTrue(contract.emitsEvent("Transfer(address,address,uint256)"));
        assertEquals(2, contract.getEventsToMonitor().size());
    }

    @Test
    void shouldCheckIfContractIsInvolvedInTransaction() {
        // Given
        SmartContract contract = new SmartContract(
            contractAddress, ethereum, contractName, abi, eventsToMonitor
        );

        Transaction transaction = new Transaction(
            dev.bloco.domain.valueobject.TransactionHash.of("0x9fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8b"),
            Address.of("0x1234567890123456789012345678901234567890"),
            contractAddress,
            ethereum,
            dev.bloco.domain.valueobject.TransactionValue.of(BigInteger.ONE),
            BigInteger.valueOf(20000000000L),
            BigInteger.valueOf(21000),
            BigInteger.valueOf(21000),
            BigInteger.valueOf(12345678),
            "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
            0,
            1,
            Instant.now(),
            true
        );

        // When & Then
        assertTrue(contract.isInvolvedIn(transaction));
    }

    @Test
    void shouldDeactivateContract() {
        // Given
        SmartContract contract = new SmartContract(
            contractAddress, ethereum, contractName, abi, eventsToMonitor
        );

        // When
        contract.deactivate();

        // Then
        assertFalse(contract.isActive());
    }

    @Test
    void shouldReactivateContract() {
        // Given
        SmartContract contract = new SmartContract(
            contractAddress, ethereum, contractName, abi, eventsToMonitor
        );
        contract.deactivate();

        // When
        contract.reactivate();

        // Then
        assertTrue(contract.isActive());
    }

    @Test
    void shouldCalculateContractAge() {
        // Given
        SmartContract contract = new SmartContract(
            contractAddress, ethereum, contractName, abi, eventsToMonitor
        );

        // When
        long ageInDays = contract.getAgeInDays();

        // Then
        assertTrue(ageInDays >= 0);
        assertTrue(ageInDays < 1); // Should be less than 1 day old
    }

    @Test
    void shouldCheckIfContractHasAbi() {
        // Given
        SmartContract contractWithAbi = new SmartContract(
            contractAddress, ethereum, contractName, abi, eventsToMonitor
        );

        SmartContract contractWithoutAbi = new SmartContract(
            Address.of("0x8ba1f109551bD432803012645261713D3e8f6c41"),
            ethereum, "NoAbiContract", "", eventsToMonitor
        );

        // When & Then
        assertTrue(contractWithAbi.hasAbi());
        assertFalse(contractWithoutAbi.hasAbi());
    }

    @Test
    void shouldGetDisplayName() {
        // Given
        SmartContract contractWithName = new SmartContract(
            contractAddress, ethereum, contractName, abi, eventsToMonitor
        );

        SmartContract contractWithoutName = new SmartContract(
            contractAddress, ethereum, "", abi, eventsToMonitor
        );

        // When & Then
        assertEquals(contractName, contractWithName.getDisplayName());
        assertEquals(contractAddress.toString(), contractWithoutName.getDisplayName());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        // Given
        SmartContract contract1 = new SmartContract(
            contractAddress, ethereum, contractName, abi, eventsToMonitor
        );

        SmartContract contract2 = new SmartContract(
            contractAddress, ethereum, "DifferentName", abi, eventsToMonitor
        );

        SmartContract differentContract = new SmartContract(
            Address.of("0x8ba1f109551bD432803012645261713D3e8f6c41"),
            ethereum, contractName, abi, eventsToMonitor
        );

        // When & Then
        assertEquals(contract1, contract2); // Same address and network
        assertEquals(contract1.hashCode(), contract2.hashCode());
        assertNotEquals(contract1, differentContract);
        assertNotEquals(contract1.hashCode(), differentContract.hashCode());
    }

    @Test
    void shouldImplementToString() {
        // Given
        SmartContract contract = new SmartContract(
            contractAddress, ethereum, contractName, abi, eventsToMonitor
        );

        // When
        String stringRepresentation = contract.toString();

        // Then
        assertTrue(stringRepresentation.contains("SmartContract"));
        assertTrue(stringRepresentation.contains(contractAddress.getValue().substring(0, 10)));
        assertTrue(stringRepresentation.contains(ethereum.getName()));
        assertTrue(stringRepresentation.contains(contractName));
    }

    @Test
    void shouldHandleNullValues() {
        // When
        SmartContract contract = new SmartContract(
            contractAddress, ethereum, null, null, null
        );

        // Then
        assertEquals("", contract.getName());
        assertEquals("", contract.getAbi());
        assertEquals(List.of(), contract.getEventsToMonitor());
    }
}