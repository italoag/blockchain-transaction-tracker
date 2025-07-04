package dev.bloco.wallet.repository;

import dev.bloco.wallet.model.Transaction;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final List<Transaction> storage = new CopyOnWriteArrayList<>();

    @Override
    public Flux<Transaction> saveAll(Flux<Transaction> transactions) {
        return transactions.doOnNext(storage::add);
    }

    public List<Transaction> findAll() {
        return List.copyOf(storage);
    }
}
