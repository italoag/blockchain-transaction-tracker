package dev.bloco.application.usecase;

import dev.bloco.domain.entity.SmartContract;
import dev.bloco.domain.entity.Transaction;
import dev.bloco.domain.entity.WalletAddress;
import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import dev.bloco.domain.valueobject.TransactionHash;
import dev.bloco.domain.valueobject.TransactionValue;
import dev.bloco.infrastructure.repository.InMemoryBlockRepository;
import dev.bloco.infrastructure.repository.InMemorySmartContractRepository;
import dev.bloco.infrastructure.repository.InMemoryTransactionRepository;
import dev.bloco.infrastructure.repository.InMemoryWalletAddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

/**
 * Integration tests for GenerateAnalyticsUseCase.
 * Tests the complete workflow with in-memory repositories.
 */
class GenerateAnalyticsUseCaseIntegrationTest {

    private GenerateAnalyticsUseCase useCase;
    private InMemoryTransactionRepository transactionRepository;
    private InMemoryWalletAddressRepository walletRepository;
    private InMemorySmartContractRepository contractRepository;
    private InMemoryBlockRepository blockRepository;

    private final Network ethereum = Network.of("ethereum", "Ethereum Mainnet", 1);
    private final Network polygon = Network.of("polygon", "Polygon Mainnet", 137);
    private final Address walletAddress1 = Address.of("0x742d35Cc6634C0532925a3b844Bc454e4438f44e");
    private final Address walletAddress2 = Address.of("0x8ba1f109551bD432803012645261713D3e8f6c41");
    private final Address contractAddress = Address.of("0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984");

    @BeforeEach
    void setUp() {
        transactionRepository = new InMemoryTransactionRepository();
        walletRepository = new InMemoryWalletAddressRepository();
        contractRepository = new InMemorySmartContractRepository();
        blockRepository = new InMemoryBlockRepository();

        useCase = new GenerateAnalyticsUseCase(
            transactionRepository,
            walletRepository,
            contractRepository,
            blockRepository
        );
    }

    @Test
    void shouldGenerateNetworkAnalyticsSuccessfully() {
        // Given
        setupTestData();

        // When
        Mono<GenerateAnalyticsUseCase.NetworkAnalytics> result = useCase.generateNetworkAnalytics(ethereum);

        // Then
        StepVerifier.create(result)
            .expectNextMatches(analytics -> {
                return analytics.getTransactionStats().getTotalTransactions() == 3 &&
                       analytics.getWalletStats().getTotalWallets() == 2 &&
                       analytics.getContractStats().getTotalContracts() == 1 &&
                       analytics.getBlockStats().getTotalBlocks() == 2;
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleEmptyData() {
        // When
        Mono<GenerateAnalyticsUseCase.NetworkAnalytics> networkAnalytics = useCase.generateNetworkAnalytics(ethereum);
        Mono<GenerateAnalyticsUseCase.SystemAnalytics> systemAnalytics = useCase.generateSystemAnalytics();
        Flux<GenerateAnalyticsUseCase.WalletActivity> topWallets = useCase.getTopActiveWallets(5);
        Mono<GenerateAnalyticsUseCase.NetworkPerformance> performance = useCase.getNetworkPerformance(ethereum);

        // Then
        StepVerifier.create(networkAnalytics)
            .expectNextMatches(analytics -> {
                return analytics.getTransactionStats().getTotalTransactions() == 0 &&
                       analytics.getWalletStats().getTotalWallets() == 0 &&
                       analytics.getContractStats().getTotalContracts() == 0 &&
                       analytics.getBlockStats().getTotalBlocks() == 0;
            })
            .verifyComplete();

        StepVerifier.create(systemAnalytics)
            .expectNextMatches(analytics -> {
                return analytics.getTotalTransactions() == 0 &&
                       analytics.getTotalWallets() == 0 &&
                       analytics.getTotalContracts() == 0 &&
                       analytics.getTotalBlocks() == 0;
            })
            .verifyComplete();

        StepVerifier.create(topWallets)
            .verifyComplete(); // No wallets

        StepVerifier.create(performance)
            .expectNextMatches(p -> {
                return p.getNetwork().equals(ethereum) &&
                       p.getAverageGasUtilization() == 0.0 &&
                       p.getBlocksPerHour() == 0 &&
                       p.getTransactionsPerHour() == 0;
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleMultipleNetworks() {
        // Given
        WalletAddress ethWallet = new WalletAddress(walletAddress1, ethereum, "ETH Wallet");
        WalletAddress polyWallet = new WalletAddress(walletAddress2, polygon, "POLY Wallet");

        walletRepository.save(ethWallet).block();
        walletRepository.save(polyWallet).block();

        Transaction ethTx = createTestTransaction(walletAddress1, contractAddress, BigInteger.valueOf(1000));
        Transaction polyTx = createTestTransaction(walletAddress2, contractAddress, BigInteger.valueOf(2000));

        transactionRepository.save(ethTx).block();
        transactionRepository.save(polyTx).block();

        SmartContract ethContract = new SmartContract(contractAddress, ethereum, "Test Contract", "[]", List.of());
        SmartContract polyContract = new SmartContract(contractAddress, polygon, "Test Contract", "[]", List.of());

        contractRepository.save(ethContract).block();
        contractRepository.save(polyContract).block();

        // When - Ethereum analytics
        Mono<GenerateAnalyticsUseCase.NetworkAnalytics> ethAnalytics = useCase.generateNetworkAnalytics(ethereum);

        // Then
        StepVerifier.create(ethAnalytics)
            .expectNextMatches(analytics -> {
                return analytics.getTransactionStats().getTotalTransactions() == 2 && // Both transactions
                       analytics.getWalletStats().getTotalWallets() == 2 && // Both wallets
                       analytics.getContractStats().getTotalContracts() == 1 && // ETH contract only
                       analytics.getBlockStats().getTotalBlocks() == 0; // No blocks
            })
            .verifyComplete();

        // When - Polygon analytics
        Mono<GenerateAnalyticsUseCase.NetworkAnalytics> polyAnalytics = useCase.generateNetworkAnalytics(polygon);

        // Then
        StepVerifier.create(polyAnalytics)
            .expectNextMatches(analytics -> {
                return analytics.getTransactionStats().getTotalTransactions() == 2 && // Both transactions
                       analytics.getWalletStats().getTotalWallets() == 2 && // Both wallets
                       analytics.getContractStats().getTotalContracts() == 1 && // POLY contract only
                       analytics.getBlockStats().getTotalBlocks() == 0; // No blocks
            })
            .verifyComplete();
    }

    private void setupTestData() {
        // Create wallets
        WalletAddress wallet1 = new WalletAddress(walletAddress1, ethereum, "Test Wallet 1");
        WalletAddress wallet2 = new WalletAddress(walletAddress2, ethereum, "Test Wallet 2");
        walletRepository.save(wallet1).block();
        walletRepository.save(wallet2).block();

        // Create transactions
        Transaction tx1 = createTestTransaction(walletAddress1, contractAddress, BigInteger.valueOf(1000));
        Transaction tx2 = createTestTransaction(walletAddress2, contractAddress, BigInteger.valueOf(2000));
        Transaction tx3 = createTestTransaction(walletAddress1, walletAddress2, BigInteger.valueOf(1500));
        transactionRepository.save(tx1).block();
        transactionRepository.save(tx2).block();
        transactionRepository.save(tx3).block();

        // Create contract
        SmartContract contract = new SmartContract(contractAddress, ethereum, "Test Contract", "[]", List.of());
        contractRepository.save(contract).block();

        // Create blocks
        dev.bloco.domain.entity.Block block1 = createTestBlock(BigInteger.valueOf(1000), walletAddress1);
        dev.bloco.domain.entity.Block block2 = createTestBlock(BigInteger.valueOf(1001), walletAddress2);
        blockRepository.save(block1).block();
        blockRepository.save(block2).block();
    }

    private Transaction createTestTransaction(Address from, Address to, BigInteger value) {
        return new Transaction(
            TransactionHash.of("0x" + String.format("%064x", value.longValue())),
            from,
            to,
            ethereum,
            TransactionValue.of(value),
            BigInteger.valueOf(20000000000L), // gasPrice
            BigInteger.valueOf(21000), // gasLimit
            BigInteger.valueOf(21000), // gasUsed
            BigInteger.valueOf(1000), // blockNumber
            "0x" + String.format("%064x", 1000L), // blockHash
            0, // transactionIndex
            1, // status
            Instant.now(),
            true // confirmed
        );
    }

    private dev.bloco.domain.entity.Block createTestBlock(BigInteger number, Address miner) {
        return createTestBlock(number, miner, Instant.now());
    }

    private dev.bloco.domain.entity.Block createTestBlock(BigInteger number, Address miner, Instant timestamp) {
        TransactionHash hash = TransactionHash.of("0x" + String.format("%064x", number.longValue()));
        TransactionHash parentHash = TransactionHash.of("0x" + String.format("%064x", number.longValue() - 1));
        List<TransactionHash> txHashes = List.of(
            TransactionHash.of("0x" + String.format("%064x", number.longValue() * 100 + 1))
        );

        return new dev.bloco.domain.entity.Block(
            number,
            hash,
            parentHash,
            miner,
            timestamp,
            ethereum,
            txHashes,
            BigInteger.valueOf(5000000), // gasUsed
            BigInteger.valueOf(10000000), // gasLimit
            true
        );
    }
}