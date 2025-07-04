package dev.bloco.wallet.service;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class BloomFilterServiceTest {

    @Test
    void addAndCheck() {
        BloomFilterService service = new BloomFilterService();
        service.add("polygon", "0x1");
        assert service.mightContain("polygon", "0x1");
    }
}
