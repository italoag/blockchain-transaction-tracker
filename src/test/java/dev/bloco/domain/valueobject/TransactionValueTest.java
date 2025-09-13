package dev.bloco.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionValue value object.
 * Tests immutability, validation, and business rules.
 */
class TransactionValueTest {

    @Test
    void shouldCreateTransactionValueWithBigInteger() {
        // Given
        BigInteger wei = BigInteger.valueOf(1000000000000000000L); // 1 ETH

        // When
        TransactionValue value = TransactionValue.of(wei);

        // Then
        assertEquals(wei, value.getWei());
    }

    @Test
    void shouldCreateTransactionValueWithString() {
        // Given
        String weiString = "1000000000000000000";

        // When
        TransactionValue value = TransactionValue.of(weiString);

        // Then
        assertEquals(new BigInteger(weiString), value.getWei());
    }

    @Test
    void shouldCreateTransactionValueWithLong() {
        // Given
        long wei = 1000000000000000000L;

        // When
        TransactionValue value = TransactionValue.of(wei);

        // Then
        assertEquals(BigInteger.valueOf(wei), value.getWei());
    }

    @Test
    void shouldCreateZeroValue() {
        // When
        TransactionValue zero = TransactionValue.zero();

        // Then
        assertEquals(BigInteger.ZERO, zero.getWei());
        assertTrue(zero.isZero());
    }

    @Test
    void shouldThrowExceptionForNullValue() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> TransactionValue.of((BigInteger) null)
        );
        assertTrue(exception.getMessage().contains("Value cannot be null"));
    }

    @Test
    void shouldThrowExceptionForNegativeValue() {
        // Given
        BigInteger negativeWei = BigInteger.valueOf(-100);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> TransactionValue.of(negativeWei)
        );
        assertTrue(exception.getMessage().contains("Value cannot be negative"));
    }

    @Test
    void shouldConvertWeiToEther() {
        // Given
        BigInteger oneEtherInWei = BigInteger.valueOf(1_000_000_000_000_000_000L);
        TransactionValue value = TransactionValue.of(oneEtherInWei);

        // When
        BigDecimal ether = value.toEther();

        // Then
        assertEquals(BigDecimal.ONE.setScale(18), ether);
    }

    @Test
    void shouldConvertWeiToGwei() {
        // Given
        BigInteger oneGweiInWei = BigInteger.valueOf(1_000_000_000L);
        TransactionValue value = TransactionValue.of(oneGweiInWei);

        // When
        BigDecimal gwei = value.toGwei();

        // Then
        assertEquals(BigDecimal.ONE.setScale(9), gwei);
    }

    @Test
    void shouldCheckIfValueIsZero() {
        // Given
        TransactionValue zero = TransactionValue.zero();
        TransactionValue positive = TransactionValue.of(BigInteger.ONE);

        // When & Then
        assertTrue(zero.isZero());
        assertFalse(positive.isZero());
    }

    @Test
    void shouldCheckIfValueIsPositive() {
        // Given
        TransactionValue zero = TransactionValue.zero();
        TransactionValue positive = TransactionValue.of(BigInteger.ONE);

        // When & Then
        assertFalse(zero.isPositive());
        assertTrue(positive.isPositive());
    }

    @Test
    void shouldAddTransactionValues() {
        // Given
        TransactionValue value1 = TransactionValue.of(BigInteger.valueOf(100));
        TransactionValue value2 = TransactionValue.of(BigInteger.valueOf(200));

        // When
        TransactionValue result = value1.add(value2);

        // Then
        assertEquals(BigInteger.valueOf(300), result.getWei());
        assertEquals(BigInteger.valueOf(100), value1.getWei()); // Original unchanged
        assertEquals(BigInteger.valueOf(200), value2.getWei()); // Original unchanged
    }

    @Test
    void shouldSubtractTransactionValues() {
        // Given
        TransactionValue value1 = TransactionValue.of(BigInteger.valueOf(300));
        TransactionValue value2 = TransactionValue.of(BigInteger.valueOf(200));

        // When
        TransactionValue result = value1.subtract(value2);

        // Then
        assertEquals(BigInteger.valueOf(100), result.getWei());
    }

    @Test
    void shouldThrowExceptionForNegativeSubtraction() {
        // Given
        TransactionValue value1 = TransactionValue.of(BigInteger.valueOf(100));
        TransactionValue value2 = TransactionValue.of(BigInteger.valueOf(200));

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> value1.subtract(value2)
        );
        assertTrue(exception.getMessage().contains("Subtraction would result in negative value"));
    }

    @Test
    void shouldMultiplyByScalar() {
        // Given
        TransactionValue value = TransactionValue.of(BigInteger.valueOf(100));

        // When
        TransactionValue result = value.multiply(5);

        // Then
        assertEquals(BigInteger.valueOf(500), result.getWei());
    }

    @Test
    void shouldThrowExceptionForNegativeScalar() {
        // Given
        TransactionValue value = TransactionValue.of(BigInteger.valueOf(100));

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> value.multiply(-1)
        );
        assertTrue(exception.getMessage().contains("Scalar cannot be negative"));
    }

    @Test
    void shouldFormatZeroValue() {
        // Given
        TransactionValue zero = TransactionValue.zero();

        // When
        String formatted = zero.toFormattedString();

        // Then
        assertEquals("0 ETH", formatted);
    }

    @Test
    void shouldFormatLargeValueInEther() {
        // Given
        BigInteger oneEtherInWei = BigInteger.valueOf(1_000_000_000_000_000_000L);
        TransactionValue value = TransactionValue.of(oneEtherInWei);

        // When
        String formatted = value.toFormattedString();

        // Then
        assertEquals("1 ETH", formatted);
    }

    @Test
    void shouldFormatSmallValueInGwei() {
        // Given
        BigInteger oneGweiInWei = BigInteger.valueOf(1_000_000_000L);
        TransactionValue value = TransactionValue.of(oneGweiInWei);

        // When
        String formatted = value.toFormattedString();

        // Then
        assertEquals("1 Gwei", formatted);
    }

    @Test
    void shouldImplementComparable() {
        // Given
        TransactionValue smaller = TransactionValue.of(BigInteger.valueOf(100));
        TransactionValue larger = TransactionValue.of(BigInteger.valueOf(200));

        // When & Then
        assertTrue(smaller.compareTo(larger) < 0);
        assertTrue(larger.compareTo(smaller) > 0);
        assertEquals(0, smaller.compareTo(smaller));
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        // Given
        BigInteger wei = BigInteger.valueOf(1000000000000000000L);
        TransactionValue value1 = TransactionValue.of(wei);
        TransactionValue value2 = TransactionValue.of(wei);
        TransactionValue differentValue = TransactionValue.of(BigInteger.valueOf(2000000000000000000L));

        // When & Then
        assertEquals(value1, value2);
        assertEquals(value1.hashCode(), value2.hashCode());
        assertNotEquals(value1, differentValue);
        assertNotEquals(value1.hashCode(), differentValue.hashCode());
    }

    @Test
    void shouldImplementToString() {
        // Given
        BigInteger wei = BigInteger.valueOf(1000000000000000000L);
        TransactionValue value = TransactionValue.of(wei);

        // When
        String stringRepresentation = value.toString();

        // Then
        assertEquals("1000000000000000000 wei", stringRepresentation);
    }

    @Test
    void shouldHandleLargeValues() {
        // Given
        BigInteger largeWei = new BigInteger("1000000000000000000000000000000000000"); // Very large number
        TransactionValue value = TransactionValue.of(largeWei);

        // When & Then
        assertEquals(largeWei, value.getWei());
        assertTrue(value.isPositive());
        assertFalse(value.isZero());
    }
}