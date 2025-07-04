package dev.bloco.wallet.repository;

import dev.bloco.wallet.model.Transaction;
import reactor.core.publisher.Flux;

public interface TransactionRepository {

    Flux<Transaction> saveAll(Flux<Transaction> transactions);
}
