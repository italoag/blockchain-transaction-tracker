package dev.bloco.wallet.service;

import dev.bloco.wallet.model.BridgeEvent;
import dev.bloco.wallet.model.Transaction;
import dev.bloco.wallet.repository.InMemoryBridgeEventRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

class BridgeEventTrackingServiceTest {

    @Test
    void detectsAndStoresEvents() {
        BlockchainConsumerService consumer = Mockito.mock(BlockchainConsumerService.class);
        Transaction tx1 = new Transaction("h1", "0xfrom", "0xbridge1", "eth", Instant.now(), true);
        Mockito.when(consumer.streamTransactions("eth")).thenReturn(Flux.just(tx1));

        BridgeDetectionService detection = new BridgeDetectionService();
        BloomFilterService bloom = new BloomFilterService();
        InMemoryBridgeEventRepository repo = new InMemoryBridgeEventRepository();
        BridgeEventTrackingService service = new BridgeEventTrackingService(consumer, detection, bloom, repo);

        Flux<BridgeEvent> events = service.watch(List.of("eth"));

        StepVerifier.create(events)
                .expectNextCount(1)
                .verifyComplete();

        Assertions.assertEquals(1, repo.findAll().size());
    }
}
