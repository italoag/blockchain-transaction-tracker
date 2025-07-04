package dev.bloco.wallet.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

class BloomFilterServiceTest {

    @Test
    void addAndCheck() {
        BloomFilterService service = new BloomFilterService();
        service.add("polygon", "0x1");
        Assertions.assertTrue(service.mightContain("polygon", "0x1"));
    }

    @Test
    void missReturnsFalse() {
        BloomFilterService service = new BloomFilterService();
        Assertions.assertFalse(service.mightContain("polygon", "0x2"));
    }
}
