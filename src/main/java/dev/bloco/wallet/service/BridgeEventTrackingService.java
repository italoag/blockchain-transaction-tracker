package dev.bloco.wallet.service;

import dev.bloco.wallet.model.BridgeEvent;
import dev.bloco.wallet.repository.BridgeEventRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class BridgeEventTrackingService {

    private final BlockchainConsumerService blockchainConsumerService;
    private final BridgeDetectionService bridgeDetectionService;
    private final BloomFilterService bloomFilterService;
    private final BridgeEventRepository bridgeEventRepository;

    public BridgeEventTrackingService(BlockchainConsumerService blockchainConsumerService,
                                      BridgeDetectionService bridgeDetectionService,
                                      BloomFilterService bloomFilterService,
                                      BridgeEventRepository bridgeEventRepository) {
        this.blockchainConsumerService = blockchainConsumerService;
        this.bridgeDetectionService = bridgeDetectionService;
        this.bloomFilterService = bloomFilterService;
        this.bridgeEventRepository = bridgeEventRepository;
    }

    public Flux<BridgeEvent> watch(List<String> networks) {
        return Flux.fromIterable(networks)
                .flatMap(network -> bridgeDetectionService.detect(
                        blockchainConsumerService.streamTransactions(network)))
                .filter(event -> {
                    if (bloomFilterService.mightContain(event.sourceNetwork(), event.hash())) {
                        return false;
                    }
                    bloomFilterService.add(event.sourceNetwork(), event.hash());
                    return true;
                })
                .transform(bridgeEventRepository::saveAll);
    }
}
