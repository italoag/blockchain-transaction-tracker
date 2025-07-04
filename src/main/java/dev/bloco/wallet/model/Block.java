package dev.bloco.wallet.model;

import java.time.Instant;
import java.util.List;

public record Block(long number,
                    Instant timestamp,
                    List<Transaction> transactions) {
}
