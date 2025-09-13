package dev.bloco.domain.entity;

import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WalletAddress domain entity.
 * Tests business logic and domain rules.
 */
class WalletAddressTest {

    private final Network ethereum = Network.of("ethereum", "Ethereum Mainnet", 1);
    private final Address address = Address.of("0x742d35Cc6634C0532925a3b844Bc454e4438f44e");
    private final String label = "My Wallet";

    @Test
    void shouldCreateWalletAddressWithValidData() {
        // When
        WalletAddress wallet = new WalletAddress(address, ethereum, label);

        // Then
        assertEquals(address, wallet.getAddress());
        assertEquals(ethereum, wallet.getNetwork());
        assertEquals(label, wallet.getLabel());
        assertEquals(WalletAddress.WalletType.UNKNOWN, wallet.getWalletType());
        assertEquals(WalletAddress.Priority.MEDIUM, wallet.getPriority());
        assertTrue(wallet.isActive());
        assertNotNull(wallet.getCreatedAt());
    }

    @Test
    void shouldCreateWalletAddressWithTypeAndPriority() {
        // When
        WalletAddress wallet = new WalletAddress(
            address, ethereum, label,
            WalletAddress.WalletType.EXCHANGE,
            WalletAddress.Priority.HIGH
        );

        // Then
        assertEquals(WalletAddress.WalletType.EXCHANGE, wallet.getWalletType());
        assertEquals(WalletAddress.Priority.HIGH, wallet.getPriority());
    }

    @Test
    void shouldDeactivateWallet() {
        // Given
        WalletAddress wallet = new WalletAddress(address, ethereum, label);

        // When
        wallet.deactivate();

        // Then
        assertFalse(wallet.isActive());
    }

    @Test
    void shouldReactivateWallet() {
        // Given
        WalletAddress wallet = new WalletAddress(address, ethereum, label);
        wallet.deactivate();

        // When
        wallet.reactivate();

        // Then
        assertTrue(wallet.isActive());
    }

    @Test
    void shouldGetDisplayName() {
        // Given
        WalletAddress walletWithLabel = new WalletAddress(address, ethereum, label);
        WalletAddress walletWithoutLabel = new WalletAddress(address, ethereum, "");

        // When & Then
        assertEquals(label, walletWithLabel.getDisplayName());
        assertEquals(address.getValue(), walletWithoutLabel.getDisplayName());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        // Given
        WalletAddress wallet1 = new WalletAddress(address, ethereum, label);
        WalletAddress wallet2 = new WalletAddress(address, ethereum, "Different Label");
        WalletAddress differentWallet = new WalletAddress(
            Address.of("0x8ba1f109551bD432803012645261713D3e8f6c41"),
            ethereum,
            label
        );

        // When & Then
        assertEquals(wallet1, wallet2); // Same address and network
        assertEquals(wallet1.hashCode(), wallet2.hashCode());
        assertNotEquals(wallet1, differentWallet);
        assertNotEquals(wallet1.hashCode(), differentWallet.hashCode());
    }

    @Test
    void shouldImplementToString() {
        // Given
        WalletAddress wallet = new WalletAddress(address, ethereum, label);

        // When
        String stringRepresentation = wallet.toString();

        // Then
        assertTrue(stringRepresentation.contains("WalletAddress"));
        assertTrue(stringRepresentation.contains(address.getValue().substring(0, 10)));
        assertTrue(stringRepresentation.contains(ethereum.getName()));
    }

    @Test
    void shouldHandleNullLabel() {
        // When
        WalletAddress wallet = new WalletAddress(address, ethereum, null);

        // Then
        assertEquals("", wallet.getLabel());
        assertEquals(address.getValue(), wallet.getDisplayName());
    }

    @Test
    void shouldCheckIfWalletHasBeenMonitoredForDays() {
        // Given
        WalletAddress wallet = new WalletAddress(address, ethereum, label);

        // When & Then
        assertFalse(wallet.hasBeenMonitoredForDays(1)); // Just created
    }
}