package dev.bloco.wallet.model;

import java.util.List;

public record TrackingRequest(String type,
                              String value,
                              List<String> networks) {
}
