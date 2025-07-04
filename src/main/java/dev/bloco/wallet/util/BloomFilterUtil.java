package dev.bloco.wallet.util;

public class BloomFilterUtil {

    private BloomFilterUtil() {
    }

    public static int hash(String value, int seed) {
        int h = value.hashCode() ^ seed;
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }
}
