package dev.bloco.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Address value object.
 * Tests immutability, validation, and business rules.
 */
class AddressTest {

    @Test
    void shouldCreateAddressWithValidValue() {
        // Given
        String validAddress = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e";

        // When
        Address address = Address.of(validAddress);

        // Then
        assertEquals(validAddress.toLowerCase(), address.getValue());
    }

    @Test
    void shouldThrowExceptionForNullValue() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Address.of(null)
        );
        assertTrue(exception.getMessage().contains("Address cannot be null"));
    }

    @Test
    void shouldThrowExceptionForEmptyValue() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Address.of("")
        );
        assertTrue(exception.getMessage().contains("Address cannot be null or empty"));
    }

    @Test
    void shouldThrowExceptionForBlankValue() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Address.of("   ")
        );
        assertTrue(exception.getMessage().contains("Address cannot be null or empty"));
    }

    @Test
    void shouldThrowExceptionForInvalidFormat() {
        // Given
        String invalidAddress = "742d35Cc6634C0532925a3b844Bc454e4438f44e"; // Missing 0x prefix

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Address.of(invalidAddress)
        );
        assertTrue(exception.getMessage().contains("Invalid address format"));
    }

    @Test
    void shouldThrowExceptionForWrongLength() {
        // Given
        String shortAddress = "0x742d35Cc6634C0532925a3b844Bc454e4438f44"; // Too short
        String longAddress = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e123"; // Too long

        // When & Then
        IllegalArgumentException shortException = assertThrows(
            IllegalArgumentException.class,
            () -> Address.of(shortAddress)
        );
        assertTrue(shortException.getMessage().contains("Invalid address format"));

        IllegalArgumentException longException = assertThrows(
            IllegalArgumentException.class,
            () -> Address.of(longAddress)
        );
        assertTrue(longException.getMessage().contains("Invalid address format"));
    }

    @Test
    void shouldThrowExceptionForInvalidHexCharacters() {
        // Given
        String invalidHexAddress = "0x742d35Cc6634C0532925a3b844Bc454e4438f44g"; // Contains 'g'

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Address.of(invalidHexAddress)
        );
        assertTrue(exception.getMessage().contains("Invalid address format"));
    }

    @Test
    void shouldNormalizeToLowerCase() {
        // Given
        String mixedCaseAddress = "0x742D35CC6634C0532925A3B844BC454E4438F44E";

        // When
        Address address = Address.of(mixedCaseAddress);

        // Then
        assertEquals(mixedCaseAddress.toLowerCase(), address.getValue());
    }

    @Test
    void shouldIdentifyContractAddress() {
        // Given
        String contractAddress = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e";
        String zeroAddress = "0x0000000000000000000000000000000000000000";

        // When
        Address contract = Address.of(contractAddress);
        Address zero = Address.of(zeroAddress);

        // Then
        assertTrue(contract.isContractAddress());
        assertFalse(zero.isContractAddress()); // Zero address is not considered a contract
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        // Given
        String addressValue = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e";
        Address address1 = Address.of(addressValue);
        Address address2 = Address.of(addressValue);
        Address differentAddress = Address.of("0x8ba1f109551bD432803012645261713D3e8f6c41");

        // When & Then
        assertEquals(address1, address2);
        assertEquals(address1.hashCode(), address2.hashCode());
        assertNotEquals(address1, differentAddress);
        assertNotEquals(address1.hashCode(), differentAddress.hashCode());
    }

    @Test
    void shouldImplementToString() {
        // Given
        String addressValue = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e";
        Address address = Address.of(addressValue);

        // When
        String stringRepresentation = address.toString();

        // Then
        assertEquals(addressValue.toLowerCase(), stringRepresentation);
    }

    @Test
    void shouldHandleChecksummedAddress() {
        // Given
        String checksummedAddress = "0x742D35CC6634C0532925A3B844BC454E4438F44E";

        // When
        Address address = Address.of(checksummedAddress);

        // Then
        assertEquals(checksummedAddress.toLowerCase(), address.getValue());
    }
}