package dev.bloco.wallet.service;

import dev.bloco.wallet.model.BridgeEvent;
import dev.bloco.wallet.model.Transaction;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class BridgeDetectionService {

    public Flux<BridgeEvent> detect(Flux<Transaction> transactions) {
        // TODO: implement bridge detection logic
        return Flux.empty();
    }
}
