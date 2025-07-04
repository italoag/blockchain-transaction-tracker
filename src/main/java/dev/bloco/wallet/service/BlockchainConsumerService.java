package dev.bloco.wallet.service;

import dev.bloco.wallet.model.Block;
import dev.bloco.wallet.model.Transaction;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class BlockchainConsumerService {

    // TODO: implement websocket connections to blockchain nodes
    public Flux<Block> streamBlocks(String network) {
        return Flux.empty();
    }

    public Flux<Transaction> streamTransactions(String network) {
        return streamBlocks(network)
                .flatMap(block -> Flux.fromIterable(block.transactions()));
    }
}
