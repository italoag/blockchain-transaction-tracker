package dev.bloco.wallet.model;

import java.time.Instant;

public record Transaction(String hash,
                          String from,
                          String to,
                          String network,
                          Instant timestamp,
                          boolean confirmed) {
}
