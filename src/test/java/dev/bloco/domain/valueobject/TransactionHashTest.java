package dev.bloco.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionHash value object.
 * Tests immutability, validation, and business rules.
 */
class TransactionHashTest {

    @Test
    void shouldCreateTransactionHashWithValidValue() {
        // Given
        String validHash = "0x9fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8b";

        // When
        TransactionHash hash = TransactionHash.of(validHash);

        // Then
        assertEquals(validHash.toLowerCase(), hash.getValue());
    }

    @Test
    void shouldThrowExceptionForNullValue() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> TransactionHash.of(null)
        );
        assertTrue(exception.getMessage().contains("Transaction hash cannot be null or empty"));
    }

    @Test
    void shouldThrowExceptionForEmptyValue() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> TransactionHash.of("")
        );
        assertTrue(exception.getMessage().contains("Transaction hash cannot be null or empty"));
    }

    @Test
    void shouldThrowExceptionForBlankValue() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> TransactionHash.of("   ")
        );
        assertTrue(exception.getMessage().contains("Transaction hash cannot be null or empty"));
    }

    @Test
    void shouldThrowExceptionForInvalidFormat() {
        // Given
        String invalidHash = "9fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8b"; // Missing 0x prefix

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> TransactionHash.of(invalidHash)
        );
        assertTrue(exception.getMessage().contains("Invalid transaction hash format"));
    }

    @Test
    void shouldThrowExceptionForWrongLength() {
        // Given
        String shortHash = "0x9fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8"; // Too short
        String longHash = "0x9fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8b123"; // Too long

        // When & Then
        IllegalArgumentException shortException = assertThrows(
            IllegalArgumentException.class,
            () -> TransactionHash.of(shortHash)
        );
        assertTrue(shortException.getMessage().contains("Invalid transaction hash format"));

        IllegalArgumentException longException = assertThrows(
            IllegalArgumentException.class,
            () -> TransactionHash.of(longHash)
        );
        assertTrue(longException.getMessage().contains("Invalid transaction hash format"));
    }

    @Test
    void shouldThrowExceptionForInvalidHexCharacters() {
        // Given
        String invalidHexHash = "0x9fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8g"; // Contains 'g'

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> TransactionHash.of(invalidHexHash)
        );
        assertTrue(exception.getMessage().contains("Invalid transaction hash format"));
    }

    @Test
    void shouldNormalizeToLowerCase() {
        // Given
        String mixedCaseHash = "0x9FC76417374AA880D4449A1F7F31EC597F00B1F6F3DD2D66F4C9C6C445836D8B";

        // When
        TransactionHash hash = TransactionHash.of(mixedCaseHash);

        // Then
        assertEquals(mixedCaseHash.toLowerCase(), hash.getValue());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        // Given
        String hashValue = "0x9fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8b";
        TransactionHash hash1 = TransactionHash.of(hashValue);
        TransactionHash hash2 = TransactionHash.of(hashValue);
        TransactionHash differentHash = TransactionHash.of("0x3fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8c");

        // When & Then
        assertEquals(hash1, hash2);
        assertEquals(hash1.hashCode(), hash2.hashCode());
        assertNotEquals(hash1, differentHash);
        assertNotEquals(hash1.hashCode(), differentHash.hashCode());
    }

    @Test
    void shouldImplementToString() {
        // Given
        String hashValue = "0x9fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8b";
        TransactionHash hash = TransactionHash.of(hashValue);

        // When
        String stringRepresentation = hash.toString();

        // Then
        assertEquals(hashValue.toLowerCase(), stringRepresentation);
    }

    @Test
    void shouldGenerateShortString() {
        // Given
        String hashValue = "0x9fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8b";
        TransactionHash hash = TransactionHash.of(hashValue);

        // When
        String shortString = hash.toShortString();

        // Then
        assertEquals("0x9fc764...5836d8b", shortString);
    }

    @Test
    void shouldHandleShortHashInToShortString() {
        // Given
        TransactionHash hash = TransactionHash.of("0x1234567890123456789012345678901234567890123456789012345678901234");

        // When
        String shortString = hash.toShortString();

        // Then
        assertEquals("0x123456...8901234", shortString);
    }

    @Test
    void shouldHandleVeryShortHashInToShortString() {
        // Given
        TransactionHash hash = TransactionHash.of("0x1234567890123456789012345678901234567890123456789012345678901234");

        // When
        String shortString = hash.toShortString();

        // Then
        assertEquals("0x123456...8901234", shortString);
    }
}