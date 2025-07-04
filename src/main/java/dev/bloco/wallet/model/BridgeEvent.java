package dev.bloco.wallet.model;

import java.time.Instant;

public record BridgeEvent(String sourceNetwork,
                          String targetNetwork,
                          String bridgeContract,
                          String hash,
                          Instant timestamp) {
}
