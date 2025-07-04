package dev.bloco.wallet.service;

import dev.bloco.wallet.model.Transaction;
import dev.bloco.wallet.model.TrackingRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class TransactionTrackingService {

    private final BlockchainConsumerService blockchainConsumerService;
    private final BloomFilterService bloomFilterService;

    public TransactionTrackingService(BlockchainConsumerService blockchainConsumerService,
                                      BloomFilterService bloomFilterService) {
        this.blockchainConsumerService = blockchainConsumerService;
        this.bloomFilterService = bloomFilterService;
    }

    public Flux<Transaction> track(TrackingRequest request) {
        return Flux.fromIterable(request.networks())
                .flatMap(network -> blockchainConsumerService.streamTransactions(network)
                        .filter(tx -> tx.from().equalsIgnoreCase(request.value())
                                || tx.to().equalsIgnoreCase(request.value())));
    }
}
