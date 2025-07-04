package dev.bloco.wallet.service;

import dev.bloco.wallet.model.Block;
import dev.bloco.wallet.model.Transaction;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class BlockchainConsumerService {

    // Simple stub that emits dummy blocks every second
    public Flux<Block> streamBlocks(String network) {
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> {
                    Instant now = Instant.now();
                    Transaction tx = new Transaction(
                            "tx-" + network + "-" + i,
                            "0xfrom", "0xto", network,
                            now, true);
                    return new Block(i, now, List.of(tx));
                });
    }

    public Flux<Transaction> streamTransactions(String network) {
        return streamBlocks(network)
                .flatMap(block -> Flux.fromIterable(block.transactions()));
    }
}
