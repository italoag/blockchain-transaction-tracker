package dev.bloco.application.usecase;

import dev.bloco.domain.entity.Block;
import dev.bloco.domain.service.BlockchainService;
import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import dev.bloco.domain.valueobject.TransactionHash;
import dev.bloco.infrastructure.repository.InMemoryBlockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Integration tests for MonitorBlocksUseCase.
 * Tests the complete workflow with in-memory repositories.
 */
class MonitorBlocksUseCaseIntegrationTest {

    private MonitorBlocksUseCase useCase;
    private InMemoryBlockRepository blockRepository;
    private BlockchainService blockchainService;

    private final Network ethereum = Network.of("ethereum", "Ethereum Mainnet", 1);
    private final Network polygon = Network.of("polygon", "Polygon Mainnet", 137);
    private final Address minerAddress = Address.of("0x5A0b54D5dc17e0AadC383d2db43B0a0D3E029c4c");
    private final Address otherMiner = Address.of("0x8ba1f109551bD432803012645261713D3e8f6c41");

    @BeforeEach
    void setUp() {
        blockRepository = new InMemoryBlockRepository();
        blockchainService = mock(BlockchainService.class);

        useCase = new MonitorBlocksUseCase(
            blockchainService,
            blockRepository
        );
    }

    @Test
    void shouldMonitorBlocksSuccessfully() {
        // Given
        Block block1 = createTestBlock(BigInteger.valueOf(1000), minerAddress);
        Block block2 = createTestBlock(BigInteger.valueOf(1001), otherMiner);

        when(blockchainService.streamBlocks(ethereum))
            .thenReturn(Flux.just(block1, block2));

        // When
        Flux<Block> result = useCase.monitorBlocks(ethereum);

        // Then
        StepVerifier.create(result)
            .expectNext(block1)
            .expectNext(block2)
            .verifyComplete();

        // Verify blocks were saved
        StepVerifier.create(blockRepository.findByNumberAndNetwork(BigInteger.valueOf(1000), ethereum))
            .expectNext(block1)
            .verifyComplete();

        StepVerifier.create(blockRepository.findByNumberAndNetwork(BigInteger.valueOf(1001), ethereum))
            .expectNext(block2)
            .verifyComplete();
    }

    @Test
    void shouldGetLatestBlock() {
        // Given
        Block block1 = createTestBlock(BigInteger.valueOf(1000), minerAddress);
        Block block2 = createTestBlock(BigInteger.valueOf(1001), minerAddress);
        Block block3 = createTestBlock(BigInteger.valueOf(1002), otherMiner);

        blockRepository.save(block1).block();
        blockRepository.save(block2).block();
        blockRepository.save(block3).block();

        // When
        Mono<Block> result = useCase.getLatestBlock(ethereum);

        // Then
        StepVerifier.create(result)
            .expectNext(block3)
            .verifyComplete();
    }

    @Test
    void shouldGetBlocksInRange() {
        // Given
        Block block1 = createTestBlock(BigInteger.valueOf(1000), minerAddress);
        Block block2 = createTestBlock(BigInteger.valueOf(1001), minerAddress);
        Block block3 = createTestBlock(BigInteger.valueOf(1002), otherMiner);
        Block block4 = createTestBlock(BigInteger.valueOf(1003), otherMiner);

        blockRepository.save(block1).block();
        blockRepository.save(block2).block();
        blockRepository.save(block3).block();
        blockRepository.save(block4).block();

        // When
        Flux<Block> result = useCase.getBlocksInRange(ethereum, BigInteger.valueOf(1001), BigInteger.valueOf(1002));

        // Then
        StepVerifier.create(result)
            .expectNext(block2)
            .expectNext(block3)
            .verifyComplete();
    }

    @Test
    void shouldGetRecentBlocks() {
        // Given
        Block oldBlock = createTestBlock(BigInteger.valueOf(1000), minerAddress,
            Instant.now().minusSeconds(3600)); // 1 hour ago
        Block recentBlock1 = createTestBlock(BigInteger.valueOf(1001), minerAddress,
            Instant.now().minusSeconds(300)); // 5 minutes ago
        Block recentBlock2 = createTestBlock(BigInteger.valueOf(1002), otherMiner,
            Instant.now().minusSeconds(60)); // 1 minute ago

        blockRepository.save(oldBlock).block();
        blockRepository.save(recentBlock1).block();
        blockRepository.save(recentBlock2).block();

        // When
        Flux<Block> result = useCase.getRecentBlocks(ethereum, 10); // Last 10 minutes

        // Then
        StepVerifier.create(result)
            .expectNext(recentBlock1)
            .expectNext(recentBlock2)
            .verifyComplete();
    }

    @Test
    void shouldGetHighGasUtilizationBlocks() {
        // Given
        Block lowGasBlock = createTestBlock(BigInteger.valueOf(1000), minerAddress,
            BigInteger.valueOf(1000000), BigInteger.valueOf(8000000)); // 12.5% utilization
        Block highGasBlock1 = createTestBlock(BigInteger.valueOf(1001), minerAddress,
            BigInteger.valueOf(9500000), BigInteger.valueOf(10000000)); // 95% utilization
        Block highGasBlock2 = createTestBlock(BigInteger.valueOf(1002), otherMiner,
            BigInteger.valueOf(9800000), BigInteger.valueOf(10000000)); // 98% utilization

        blockRepository.save(lowGasBlock).block();
        blockRepository.save(highGasBlock1).block();
        blockRepository.save(highGasBlock2).block();

        // When
        Flux<Block> result = useCase.getHighGasUtilizationBlocks(ethereum);

        // Then
        StepVerifier.create(result)
            .expectNext(highGasBlock1)
            .expectNext(highGasBlock2)
            .verifyComplete();
    }

    @Test
    void shouldGetNetworkBlockStats() {
        // Given
        Block block1 = createTestBlock(BigInteger.valueOf(1000), minerAddress,
            BigInteger.valueOf(5000000), BigInteger.valueOf(10000000)); // 50% utilization
        Block block2 = createTestBlock(BigInteger.valueOf(1001), minerAddress,
            BigInteger.valueOf(8000000), BigInteger.valueOf(10000000)); // 80% utilization
        Block block3 = createTestBlock(BigInteger.valueOf(1002), otherMiner,
            BigInteger.valueOf(9000000), BigInteger.valueOf(10000000)); // 90% utilization

        blockRepository.save(block1).block();
        blockRepository.save(block2).block();
        blockRepository.save(block3).block();

        // When
        Mono<MonitorBlocksUseCase.BlockStats> result = useCase.getNetworkBlockStats(ethereum);

        // Then
        StepVerifier.create(result)
            .expectNextMatches(stats -> {
                return stats.getTotalBlocks() == 3 &&
                       stats.getLatestBlockNumber().equals(BigInteger.valueOf(1002)) &&
                       Math.abs(stats.getAverageGasUtilization() - 73.33) < 1.0 && // (50+80+90)/3 ≈ 73.33
                       stats.getUniqueMiners() == 2;
            })
            .verifyComplete();
    }

    @Test
    void shouldDetectNetworkCongestion() {
        // Given - 6 blocks with high gas utilization (60% of recent blocks)
        for (int i = 0; i < 6; i++) {
            Block highGasBlock = createTestBlock(BigInteger.valueOf(1000 + i), minerAddress,
                BigInteger.valueOf(9500000), BigInteger.valueOf(10000000)); // 95% utilization
            blockRepository.save(highGasBlock).block();
        }

        // Add 4 blocks with low gas utilization
        for (int i = 6; i < 10; i++) {
            Block lowGasBlock = createTestBlock(BigInteger.valueOf(1000 + i), otherMiner,
                BigInteger.valueOf(1000000), BigInteger.valueOf(10000000)); // 10% utilization
            blockRepository.save(lowGasBlock).block();
        }

        // When
        Mono<Boolean> result = useCase.isNetworkCongested(ethereum);

        // Then
        StepVerifier.create(result)
            .expectNext(true) // 6 out of 10 blocks have high gas utilization (60% >= 50%)
            .verifyComplete();
    }

    @Test
    void shouldNotDetectNetworkCongestionWhenBelowThreshold() {
        // Given - 4 blocks with high gas utilization (40% of recent blocks)
        for (int i = 0; i < 4; i++) {
            Block highGasBlock = createTestBlock(BigInteger.valueOf(1000 + i), minerAddress,
                BigInteger.valueOf(9500000), BigInteger.valueOf(10000000)); // 95% utilization
            blockRepository.save(highGasBlock).block();
        }

        // Add 6 blocks with low gas utilization
        for (int i = 4; i < 10; i++) {
            Block lowGasBlock = createTestBlock(BigInteger.valueOf(1000 + i), otherMiner,
                BigInteger.valueOf(1000000), BigInteger.valueOf(10000000)); // 10% utilization
            blockRepository.save(lowGasBlock).block();
        }

        // When
        Mono<Boolean> result = useCase.isNetworkCongested(ethereum);

        // Then
        StepVerifier.create(result)
            .expectNext(false) // 4 out of 10 blocks have high gas utilization (40% < 50%)
            .verifyComplete();
    }

    @Test
    void shouldCalculateAverageBlockTime() {
        // Given - blocks with known time differences
        Instant baseTime = Instant.now();
        Block block1 = createTestBlock(BigInteger.valueOf(1000), minerAddress, baseTime);
        Block block2 = createTestBlock(BigInteger.valueOf(1001), minerAddress, baseTime.plusSeconds(15));
        Block block3 = createTestBlock(BigInteger.valueOf(1002), minerAddress, baseTime.plusSeconds(30));
        Block block4 = createTestBlock(BigInteger.valueOf(1003), minerAddress, baseTime.plusSeconds(45));

        blockRepository.save(block1).block();
        blockRepository.save(block2).block();
        blockRepository.save(block3).block();
        blockRepository.save(block4).block();

        // When
        Mono<Double> result = useCase.getAverageBlockTime(ethereum);

        // Then
        StepVerifier.create(result)
            .expectNextMatches(avgTime -> Math.abs(avgTime - 15.0) < 0.1) // Average of 15s differences
            .verifyComplete();
    }

    @Test
    void shouldHandleEmptyRepository() {
        // When
        Mono<Block> latestBlock = useCase.getLatestBlock(ethereum);
        Flux<Block> blocksInRange = useCase.getBlocksInRange(ethereum, BigInteger.ONE, BigInteger.TEN);
        Flux<Block> recentBlocks = useCase.getRecentBlocks(ethereum, 10);
        Flux<Block> highGasBlocks = useCase.getHighGasUtilizationBlocks(ethereum);
        Mono<MonitorBlocksUseCase.BlockStats> stats = useCase.getNetworkBlockStats(ethereum);
        Mono<Boolean> congestion = useCase.isNetworkCongested(ethereum);
        Mono<Double> avgBlockTime = useCase.getAverageBlockTime(ethereum);

        // Then
        StepVerifier.create(latestBlock)
            .verifyComplete(); // No latest block

        StepVerifier.create(blocksInRange)
            .verifyComplete(); // No blocks in range

        StepVerifier.create(recentBlocks)
            .verifyComplete(); // No recent blocks

        StepVerifier.create(highGasBlocks)
            .verifyComplete(); // No high gas blocks

        StepVerifier.create(stats)
            .expectNextMatches(s -> s.getTotalBlocks() == 0 && s.getUniqueMiners() == 0)
            .verifyComplete();

        StepVerifier.create(congestion)
            .expectNext(false) // Not congested with no blocks
            .verifyComplete();

        StepVerifier.create(avgBlockTime)
            .expectNext(0.0) // No block time with insufficient blocks
            .verifyComplete();
    }

    @Test
    void shouldHandleMultipleNetworks() {
        // Given
        Block ethBlock = createTestBlock(BigInteger.valueOf(1000), minerAddress);
        Block polyBlock = createTestBlock(BigInteger.valueOf(2000), otherMiner, polygon);

        blockRepository.save(ethBlock).block();
        blockRepository.save(polyBlock).block();

        // When - Query Ethereum
        Mono<Block> ethLatest = useCase.getLatestBlock(ethereum);
        Flux<Block> ethRecent = useCase.getRecentBlocks(ethereum, 10);

        // Then
        StepVerifier.create(ethLatest)
            .expectNext(ethBlock)
            .verifyComplete();

        StepVerifier.create(ethRecent)
            .expectNext(ethBlock)
            .verifyComplete();

        // When - Query Polygon
        Mono<Block> polyLatest = useCase.getLatestBlock(polygon);
        Flux<Block> polyRecent = useCase.getRecentBlocks(polygon, 10);

        // Then
        StepVerifier.create(polyLatest)
            .expectNext(polyBlock)
            .verifyComplete();

        StepVerifier.create(polyRecent)
            .expectNext(polyBlock)
            .verifyComplete();
    }

    private Block createTestBlock(BigInteger number, Address miner) {
        return createTestBlock(number, miner, Instant.now());
    }

    private Block createTestBlock(BigInteger number, Address miner, Instant timestamp) {
        return createTestBlock(number, miner, timestamp, BigInteger.valueOf(5000000), BigInteger.valueOf(10000000));
    }

    private Block createTestBlock(BigInteger number, Address miner, Network network) {
        return createTestBlock(number, miner, Instant.now(), network);
    }

    private Block createTestBlock(BigInteger number, Address miner, Instant timestamp, Network network) {
        return createTestBlock(number, miner, timestamp, BigInteger.valueOf(5000000), BigInteger.valueOf(10000000), network);
    }

    private Block createTestBlock(BigInteger number, Address miner, BigInteger gasUsed, BigInteger gasLimit) {
        return createTestBlock(number, miner, Instant.now(), gasUsed, gasLimit);
    }

    private Block createTestBlock(BigInteger number, Address miner, Instant timestamp, BigInteger gasUsed, BigInteger gasLimit) {
        return createTestBlock(number, miner, timestamp, gasUsed, gasLimit, ethereum);
    }

    private Block createTestBlock(BigInteger number, Address miner, Instant timestamp, BigInteger gasUsed, BigInteger gasLimit, Network network) {
        TransactionHash hash = TransactionHash.of("0x" + String.format("%064x", number.longValue()));
        TransactionHash parentHash = TransactionHash.of("0x" + String.format("%064x", number.longValue() - 1));
        List<TransactionHash> txHashes = List.of(
            TransactionHash.of("0x" + String.format("%064x", number.longValue() * 100 + 1)),
            TransactionHash.of("0x" + String.format("%064x", number.longValue() * 100 + 2))
        );

        return new Block(number, hash, parentHash, miner, timestamp, network, txHashes, gasUsed, gasLimit, true);
    }
}