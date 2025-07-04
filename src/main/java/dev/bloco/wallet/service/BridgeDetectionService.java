package dev.bloco.wallet.service;

import dev.bloco.wallet.model.BridgeEvent;
import dev.bloco.wallet.model.Transaction;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Locale;

@Service
public class BridgeDetectionService {

    public Flux<BridgeEvent> detect(Flux<Transaction> transactions) {
        return transactions
                .filter(tx -> {
                    String from = tx.from().toLowerCase(Locale.ROOT);
                    String to = tx.to().toLowerCase(Locale.ROOT);
                    return from.contains("bridge") || to.contains("bridge");
                })
                .map(tx -> new BridgeEvent(
                        tx.network(),
                        tx.network(),
                        tx.to(),
                        tx.hash(),
                        tx.timestamp()
                ));
    }
}
