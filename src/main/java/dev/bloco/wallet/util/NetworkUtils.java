package dev.bloco.wallet.util;

import java.time.Duration;

public class NetworkUtils {

    private NetworkUtils() {
    }

    public static Duration reconnectBackoff(int attempts) {
        long seconds = Math.min(60, (long) Math.pow(2, attempts));
        return Duration.ofSeconds(seconds);
    }
}
