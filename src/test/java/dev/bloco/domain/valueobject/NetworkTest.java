package dev.bloco.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Network value object.
 * Tests immutability, validation, and business rules.
 */
class NetworkTest {

    @Test
    void shouldCreateNetworkWithValidData() {
        // Given
        String id = "ethereum";
        String name = "Ethereum Mainnet";
        int chainId = 1;

        // When
        Network network = Network.of(id, name, chainId);

        // Then
        assertEquals(id.toLowerCase(), network.getId());
        assertEquals(name, network.getName());
        assertEquals(chainId, network.getChainId());
    }

    @Test
    void shouldThrowExceptionForNullId() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Network.of(null, "Ethereum", 1)
        );
        assertTrue(exception.getMessage().contains("Network ID cannot be null or empty"));
    }

    @Test
    void shouldThrowExceptionForEmptyId() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Network.of("", "Ethereum", 1)
        );
        assertTrue(exception.getMessage().contains("Network ID cannot be null or empty"));
    }

    @Test
    void shouldThrowExceptionForBlankId() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Network.of("   ", "Ethereum", 1)
        );
        assertTrue(exception.getMessage().contains("Network ID cannot be null or empty"));
    }

    @Test
    void shouldThrowExceptionForNullName() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Network.of("ethereum", null, 1)
        );
        assertTrue(exception.getMessage().contains("Network name cannot be null or empty"));
    }

    @Test
    void shouldThrowExceptionForEmptyName() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Network.of("ethereum", "", 1)
        );
        assertTrue(exception.getMessage().contains("Network name cannot be null or empty"));
    }

    @Test
    void shouldNormalizeIdToLowerCase() {
        // Given
        String mixedCaseId = "Ethereum";
        String name = "Ethereum Mainnet";

        // When
        Network network = Network.of(mixedCaseId, name, 1);

        // Then
        assertEquals(mixedCaseId.toLowerCase(), network.getId());
    }

    @Test
    void shouldCreateEthereumNetwork() {
        // When
        Network ethereum = Network.ethereum();

        // Then
        assertEquals("ethereum", ethereum.getId());
        assertEquals("Ethereum", ethereum.getName());
        assertEquals(1, ethereum.getChainId());
    }

    @Test
    void shouldCreatePolygonNetwork() {
        // When
        Network polygon = Network.polygon();

        // Then
        assertEquals("polygon", polygon.getId());
        assertEquals("Polygon", polygon.getName());
        assertEquals(137, polygon.getChainId());
    }

    @Test
    void shouldCreateBscNetwork() {
        // When
        Network bsc = Network.bsc();

        // Then
        assertEquals("bsc", bsc.getId());
        assertEquals("Binance Smart Chain", bsc.getName());
        assertEquals(56, bsc.getChainId());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        // Given
        Network network1 = Network.of("ethereum", "Ethereum Mainnet", 1);
        Network network2 = Network.of("ethereum", "Ethereum Mainnet", 1);
        Network differentNetwork = Network.of("polygon", "Polygon", 137);

        // When & Then
        assertEquals(network1, network2);
        assertEquals(network1.hashCode(), network2.hashCode());
        assertNotEquals(network1, differentNetwork);
        assertNotEquals(network1.hashCode(), differentNetwork.hashCode());
    }

    @Test
    void shouldImplementToString() {
        // Given
        Network network = Network.of("ethereum", "Ethereum Mainnet", 1);

        // When
        String stringRepresentation = network.toString();

        // Then
        assertTrue(stringRepresentation.contains("Network"));
        assertTrue(stringRepresentation.contains("ethereum"));
        assertTrue(stringRepresentation.contains("Ethereum Mainnet"));
        assertTrue(stringRepresentation.contains("1"));
    }

    @Test
    void shouldHandleDifferentChainIds() {
        // Given
        Network mainnet = Network.of("ethereum", "Ethereum Mainnet", 1);
        Network testnet = Network.of("ethereum", "Ethereum Testnet", 5);

        // When & Then
        assertNotEquals(mainnet, testnet);
        assertNotEquals(mainnet.hashCode(), testnet.hashCode());
    }

    @Test
    void shouldHandleDifferentNetworkNames() {
        // Given
        Network network1 = Network.of("ethereum", "Ethereum Mainnet", 1);
        Network network2 = Network.of("ethereum", "Ethereum Network", 1);

        // When & Then
        assertNotEquals(network1, network2);
        assertNotEquals(network1.hashCode(), network2.hashCode());
    }
}