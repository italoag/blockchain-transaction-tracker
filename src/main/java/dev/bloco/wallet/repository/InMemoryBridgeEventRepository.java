package dev.bloco.wallet.repository;

import dev.bloco.wallet.model.BridgeEvent;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryBridgeEventRepository implements BridgeEventRepository {

    private final List<BridgeEvent> storage = new CopyOnWriteArrayList<>();

    @Override
    public Flux<BridgeEvent> saveAll(Flux<BridgeEvent> events) {
        return events.doOnNext(storage::add);
    }

    public List<BridgeEvent> findAll() {
        return List.copyOf(storage);
    }
}
