package dev.bloco.wallet.service;

import dev.bloco.wallet.model.Transaction;
import dev.bloco.wallet.model.TrackingRequest;
import dev.bloco.wallet.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class TransactionTrackingService {

    private final BlockchainConsumerService blockchainConsumerService;
    private final BloomFilterService bloomFilterService;
    private final TransactionRepository transactionRepository;

    public TransactionTrackingService(BlockchainConsumerService blockchainConsumerService,
                                      BloomFilterService bloomFilterService,
                                      TransactionRepository transactionRepository) {
        this.blockchainConsumerService = blockchainConsumerService;
        this.bloomFilterService = bloomFilterService;
        this.transactionRepository = transactionRepository;
    }

    public Flux<Transaction> track(TrackingRequest request) {
        return Flux.fromIterable(request.networks())
                .flatMap(network -> blockchainConsumerService.streamTransactions(network)
                        .filter(tx -> tx.from().equalsIgnoreCase(request.value())
                                || tx.to().equalsIgnoreCase(request.value()))
                        .filter(tx -> {
                            if (bloomFilterService.mightContain(tx.network(), tx.hash())) {
                                return false;
                            }
                            bloomFilterService.add(tx.network(), tx.hash());
                            return true;
                        }))
                .transform(transactionRepository::saveAll);
    }
}
