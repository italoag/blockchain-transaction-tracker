package dev.bloco.infrastructure.repository;

import dev.bloco.domain.entity.Block;
import dev.bloco.domain.repository.BlockRepository;
import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import dev.bloco.domain.valueobject.TransactionHash;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory implementation of BlockRepository.
 * Thread-safe implementation using ConcurrentHashMap for storage.
 */
public class InMemoryBlockRepository implements BlockRepository {

    private final ConcurrentMap<String, Block> storage = new ConcurrentHashMap<>();

    @Override
    public Mono<Block> save(Block block) {
        return Mono.fromCallable(() -> {
            String key = createKey(block.getNumber(), block.getNetwork());
            storage.put(key, block);
            return block;
        });
    }

    @Override
    public Mono<Block> findByNumberAndNetwork(BigInteger number, Network network) {
        return Mono.fromCallable(() -> {
            String key = createKey(number, network);
            return storage.get(key);
        });
    }

    @Override
    public Mono<Block> findByHashAndNetwork(TransactionHash hash, Network network) {
        return Flux.fromIterable(storage.values())
            .filter(block -> block.getHash().equals(hash) && block.getNetwork().equals(network))
            .next();
    }

    @Override
    public Mono<Block> findLatestBlock(Network network) {
        return Flux.fromIterable(storage.values())
            .filter(block -> block.getNetwork().equals(network))
            .reduce((block1, block2) -> block1.getNumber().compareTo(block2.getNumber()) > 0 ? block1 : block2);
    }

    @Override
    public Flux<Block> findByNumberRange(BigInteger startNumber, BigInteger endNumber, Network network) {
        return Flux.fromIterable(storage.values())
            .filter(block -> block.getNetwork().equals(network))
            .filter(block -> {
                BigInteger number = block.getNumber();
                return number.compareTo(startNumber) >= 0 && number.compareTo(endNumber) <= 0;
            })
            .sort((block1, block2) -> block1.getNumber().compareTo(block2.getNumber()));
    }

    @Override
    public Flux<Block> findByMiner(Address miner, Network network) {
        return Flux.fromIterable(storage.values())
            .filter(block -> block.getNetwork().equals(network))
            .filter(block -> block.getMiner().equals(miner));
    }

    @Override
    public Flux<Block> findByTimeRange(Instant startTime, Instant endTime, Network network) {
        return Flux.fromIterable(storage.values())
            .filter(block -> block.getNetwork().equals(network))
            .filter(block -> {
                Instant timestamp = block.getTimestamp();
                return timestamp.isAfter(startTime) && timestamp.isBefore(endTime);
            });
    }

    @Override
    public Flux<Block> findRecentBlocks(Network network, int minutes) {
        Instant cutoffTime = Instant.now().minusSeconds(minutes * 60L);
        return Flux.fromIterable(storage.values())
            .filter(block -> block.getNetwork().equals(network))
            .filter(block -> block.getTimestamp().isAfter(cutoffTime));
    }

    @Override
    public Flux<Block> findHighGasUtilizationBlocks(Network network) {
        return Flux.fromIterable(storage.values())
            .filter(block -> block.getNetwork().equals(network))
            .filter(Block::hasHighGasUtilization);
    }

    @Override
    public Flux<Block> findBlocksContainingTransaction(TransactionHash transactionHash, Network network) {
        return Flux.fromIterable(storage.values())
            .filter(block -> block.getNetwork().equals(network))
            .filter(block -> block.getTransactionHashes().contains(transactionHash));
    }

    @Override
    public Mono<Boolean> existsByNumberAndNetwork(BigInteger number, Network network) {
        return Mono.fromCallable(() -> {
            String key = createKey(number, network);
            return storage.containsKey(key);
        });
    }

    @Override
    public Mono<Long> countByNetwork(Network network) {
        return Flux.fromIterable(storage.values())
            .filter(block -> block.getNetwork().equals(network))
            .count();
    }

    @Override
    public Mono<Long> countByMiner(Address miner, Network network) {
        return Flux.fromIterable(storage.values())
            .filter(block -> block.getNetwork().equals(network))
            .filter(block -> block.getMiner().equals(miner))
            .count();
    }

    @Override
    public Mono<Double> getAverageGasUtilization(Network network) {
        return Flux.fromIterable(storage.values())
            .filter(block -> block.getNetwork().equals(network))
            .map(Block::getGasUtilizationPercentage)
            .collectList()
            .map(utilizations -> {
                if (utilizations.isEmpty()) return 0.0;
                return utilizations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            });
    }

    @Override
    public Mono<Double> getAverageGasUtilization(Network network, Instant startTime, Instant endTime) {
        return Flux.fromIterable(storage.values())
            .filter(block -> block.getNetwork().equals(network))
            .filter(block -> {
                Instant timestamp = block.getTimestamp();
                return timestamp.isAfter(startTime) && timestamp.isBefore(endTime);
            })
            .map(Block::getGasUtilizationPercentage)
            .collectList()
            .map(utilizations -> {
                if (utilizations.isEmpty()) return 0.0;
                return utilizations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            });
    }

    @Override
    public Mono<BlockStats> getBlockStats(Network network) {
        return Flux.fromIterable(storage.values())
            .filter(block -> block.getNetwork().equals(network))
            .collectList()
            .map(blocks -> {
                if (blocks.isEmpty()) {
                    return new BlockStats(0, BigInteger.ZERO, 0.0, 0, Instant.now(), Instant.now());
                }

                long totalBlocks = blocks.size();
                BigInteger latestBlockNumber = blocks.stream()
                    .map(Block::getNumber)
                    .max(BigInteger::compareTo)
                    .orElse(BigInteger.ZERO);

                double averageGasUtilization = blocks.stream()
                    .mapToDouble(Block::getGasUtilizationPercentage)
                    .average()
                    .orElse(0.0);

                long uniqueMiners = blocks.stream()
                    .map(Block::getMiner)
                    .distinct()
                    .count();

                Instant oldestBlock = blocks.stream()
                    .map(Block::getTimestamp)
                    .min(Instant::compareTo)
                    .orElse(Instant.now());

                Instant newestBlock = blocks.stream()
                    .map(Block::getTimestamp)
                    .max(Instant::compareTo)
                    .orElse(Instant.now());

                return new BlockStats(totalBlocks, latestBlockNumber, averageGasUtilization,
                                    uniqueMiners, oldestBlock, newestBlock);
            });
    }

    @Override
    public Mono<Void> deleteByNumberAndNetwork(BigInteger number, Network network) {
        return Mono.fromRunnable(() -> {
            String key = createKey(number, network);
            storage.remove(key);
        });
    }

    @Override
    public Mono<Long> deleteOlderThan(int minutes, Network network) {
        Instant cutoffTime = Instant.now().minusSeconds(minutes * 60L);
        return Flux.fromIterable(storage.entrySet())
            .filter(entry -> {
                Block block = entry.getValue();
                return block.getNetwork().equals(network) && block.getTimestamp().isBefore(cutoffTime);
            })
            .map(entry -> {
                storage.remove(entry.getKey());
                return 1L;
            })
            .count();
    }

    private String createKey(BigInteger number, Network network) {
        return network.getName() + ":" + number.toString();
    }
}