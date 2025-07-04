package dev.bloco.wallet.service;

import dev.bloco.wallet.model.Transaction;
import dev.bloco.wallet.model.TrackingRequest;
import dev.bloco.wallet.repository.InMemoryTransactionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

class TransactionTrackingServiceTest {

    @Test
    void tracksAndStoresTransactions() {
        BlockchainConsumerService consumer = Mockito.mock(BlockchainConsumerService.class);
        Transaction tx1 = new Transaction("h1", "0xA", "0xB", "eth", Instant.now(), true);
        Transaction tx2 = new Transaction("h2", "0xB", "0xA", "eth", Instant.now(), true);
        Mockito.when(consumer.streamTransactions("eth")).thenReturn(Flux.just(tx1, tx2));

        BloomFilterService bloom = new BloomFilterService();
        InMemoryTransactionRepository repo = new InMemoryTransactionRepository();
        TransactionTrackingService service = new TransactionTrackingService(consumer, bloom, repo);

        TrackingRequest req = new TrackingRequest("address", "0xA", List.of("eth"));
        Flux<Transaction> result = service.track(req);

        StepVerifier.create(result)
                .expectNext(tx1)
                .expectNext(tx2)
                .verifyComplete();

        Assertions.assertEquals(2, repo.findAll().size());
    }
}
