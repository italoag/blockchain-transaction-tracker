package dev.bloco.wallet.repository;

import dev.bloco.wallet.model.BridgeEvent;
import reactor.core.publisher.Flux;

public interface BridgeEventRepository {
    Flux<BridgeEvent> saveAll(Flux<BridgeEvent> events);
}
