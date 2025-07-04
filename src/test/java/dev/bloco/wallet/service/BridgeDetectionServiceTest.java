package dev.bloco.wallet.service;

import dev.bloco.wallet.model.Transaction;
import dev.bloco.wallet.model.BridgeEvent;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Instant;

class BridgeDetectionServiceTest {

    @Test
    void detectsBridgeTransactions() {
        BridgeDetectionService service = new BridgeDetectionService();
        Transaction tx1 = new Transaction("hash1", "0xA", "0xbridge", "ethereum", Instant.now(), true);
        Transaction tx2 = new Transaction("hash2", "0xB", "0xC", "ethereum", Instant.now(), true);

        Flux<BridgeEvent> events = service.detect(Flux.just(tx1, tx2));

        StepVerifier.create(events)
                .expectNextMatches(e -> e.hash().equals("hash1") && e.bridgeContract().equals("0xbridge"))
                .verifyComplete();
    }
}
