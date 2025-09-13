package dev.bloco.application.usecase;

import dev.bloco.domain.entity.Transaction;
import dev.bloco.domain.entity.WalletAddress;
import dev.bloco.domain.service.BlockchainService;
import dev.bloco.domain.service.DeduplicationService;
import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import dev.bloco.domain.valueobject.TransactionHash;
import dev.bloco.domain.valueobject.TransactionValue;
import dev.bloco.infrastructure.repository.InMemoryTransactionRepository;
import dev.bloco.infrastructure.repository.InMemoryWalletAddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigInteger;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for TrackWalletTransactionsUseCase.
 * Tests the complete workflow with in-memory repositories.
 */
class TrackWalletTransactionsUseCaseIntegrationTest {

    private TrackWalletTransactionsUseCase useCase;
    private InMemoryTransactionRepository transactionRepository;
    private InMemoryWalletAddressRepository walletRepository;
    private BlockchainService blockchainService;
    private DeduplicationService deduplicationService;

    private final Network ethereum = Network.of("ethereum", "Ethereum Mainnet", 1);
    private final Address walletAddress = Address.of("0x742d35Cc6634C0532925a3b844Bc454e4438f44e");
    private final Address otherAddress = Address.of("0x8ba1f109551bD432803012645261713D3e8f6c41");

    @BeforeEach
    void setUp() {
        transactionRepository = new InMemoryTransactionRepository();
        walletRepository = new InMemoryWalletAddressRepository();
        blockchainService = mock(BlockchainService.class);
        deduplicationService = mock(DeduplicationService.class);

        useCase = new TrackWalletTransactionsUseCase(
            blockchainService,
            transactionRepository,
            walletRepository,
            deduplicationService
        );
    }

    @Test
    void shouldTrackWalletTransactionsSuccessfully() {
        // Given
        WalletAddress wallet = new WalletAddress(walletAddress, ethereum, "Test Wallet");
        walletRepository.save(wallet).block();

        Transaction transaction = createTestTransaction(walletAddress, otherAddress);
        when(blockchainService.getTransactionsForAddress(walletAddress, ethereum))
            .thenReturn(Flux.just(transaction));
        when(deduplicationService.mightContain(anyString(), anyString())).thenReturn(false);

        // When
        Flux<Transaction> result = useCase.trackWallet(walletAddress, ethereum);

        // Then
        StepVerifier.create(result)
            .expectNext(transaction)
            .verifyComplete();

        verify(deduplicationService).add(ethereum.getName(), transaction.getHash().getValue());
    }

    @Test
    void shouldSkipInactiveWallet() {
        // Given
        WalletAddress wallet = new WalletAddress(walletAddress, ethereum, "Test Wallet");
        wallet.deactivate();
        walletRepository.save(wallet).block();

        // When
        Flux<Transaction> result = useCase.trackWallet(walletAddress, ethereum);

        // Then
        StepVerifier.create(result)
            .verifyComplete();

        verify(blockchainService, never()).getTransactionsForAddress(any(), any());
    }

    @Test
    void shouldSkipDuplicateTransactions() {
        // Given
        WalletAddress wallet = new WalletAddress(walletAddress, ethereum, "Test Wallet");
        walletRepository.save(wallet).block();

        Transaction transaction = createTestTransaction(walletAddress, otherAddress);
        when(blockchainService.getTransactionsForAddress(walletAddress, ethereum))
            .thenReturn(Flux.just(transaction));
        when(deduplicationService.mightContain(ethereum.getName(), transaction.getHash().getValue()))
            .thenReturn(true);

        // When
        Flux<Transaction> result = useCase.trackWallet(walletAddress, ethereum);

        // Then
        StepVerifier.create(result)
            .verifyComplete();

        verify(deduplicationService, never()).add(anyString(), anyString());
    }

    @Test
    void shouldTrackAllActiveWallets() {
        // Given
        WalletAddress activeWallet = new WalletAddress(walletAddress, ethereum, "Active Wallet");
        WalletAddress inactiveWallet = new WalletAddress(otherAddress, ethereum, "Inactive Wallet");
        inactiveWallet.deactivate();

        walletRepository.save(activeWallet).block();
        walletRepository.save(inactiveWallet).block();

        Transaction transaction = createTestTransaction(walletAddress, otherAddress);
        when(blockchainService.getTransactionsForAddress(walletAddress, ethereum))
            .thenReturn(Flux.just(transaction));
        when(deduplicationService.mightContain(anyString(), anyString())).thenReturn(false);

        // When
        Flux<Transaction> result = useCase.trackAllActiveWallets();

        // Then
        StepVerifier.create(result)
            .expectNext(transaction)
            .verifyComplete();
    }

    @Test
    void shouldTrackHighPriorityWallets() {
        // Given
        WalletAddress highPriorityWallet = new WalletAddress(
            walletAddress, ethereum, "High Priority",
            WalletAddress.WalletType.EXCHANGE, WalletAddress.Priority.HIGH
        );
        WalletAddress normalWallet = new WalletAddress(
            otherAddress, ethereum, "Normal Priority"
        );

        walletRepository.save(highPriorityWallet).block();
        walletRepository.save(normalWallet).block();

        Transaction transaction = createTestTransaction(walletAddress, otherAddress);
        when(blockchainService.getTransactionsForAddress(walletAddress, ethereum))
            .thenReturn(Flux.just(transaction));
        when(deduplicationService.mightContain(anyString(), anyString())).thenReturn(false);

        // When
        Flux<Transaction> result = useCase.trackHighPriorityWallets();

        // Then
        StepVerifier.create(result)
            .expectNext(transaction)
            .verifyComplete();
    }

    @Test
    void shouldGetRecentWalletTransactions() {
        // Given
        Transaction recentTransaction = createTestTransaction(walletAddress, otherAddress);
        Transaction oldTransaction = createTestTransaction(walletAddress, otherAddress, Instant.now().minusSeconds(7200));

        transactionRepository.saveAll(Flux.just(recentTransaction, oldTransaction)).blockLast();

        // When
        Flux<Transaction> result = useCase.getRecentWalletTransactions(walletAddress, ethereum, 60);

        // Then
        StepVerifier.create(result)
            .expectNext(recentTransaction)
            .verifyComplete();
    }

    @Test
    void shouldGetWalletTransactionStats() {
        // Given
        Transaction transaction1 = createTestTransaction(walletAddress, otherAddress,
            TransactionValue.of(BigInteger.valueOf(1000000000000000000L)), // 1 ETH
            Instant.now().minusSeconds(7200)); // 2 hours ago
        Transaction transaction2 = createTestTransaction(walletAddress, otherAddress,
            TransactionValue.of(BigInteger.valueOf(2000000000000000000L)), // 2 ETH
            Instant.now().minusSeconds(3600)); // 1 hour ago
        Transaction recentTransaction = createTestTransaction(walletAddress, otherAddress,
            TransactionValue.of(BigInteger.valueOf(500000000000000000L)), // 0.5 ETH
            Instant.now()); // now

        transactionRepository.saveAll(Flux.just(transaction1, transaction2)).blockLast();
        transactionRepository.save(recentTransaction).block();

        // When
        Mono<TrackWalletTransactionsUseCase.WalletTransactionStats> result =
            useCase.getWalletStats(walletAddress, ethereum);

        // Then
        StepVerifier.create(result)
            .assertNext(stats -> {
                assert stats.getTotalTransactions() == 3 : "Expected 3 total transactions, got " + stats.getTotalTransactions();
                assert stats.getRecentTransactions() == 1 : "Expected 1 recent transaction, got " + stats.getRecentTransactions();
                assert stats.getTotalValue().equals(BigInteger.valueOf(3500000000000000000L)) :
                    "Expected 3.5 ETH, got " + stats.getTotalValue();
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleWalletNotFound() {
        // When
        Flux<Transaction> result = useCase.trackWallet(walletAddress, ethereum);

        // Then
        StepVerifier.create(result)
            .verifyComplete();

        verify(blockchainService, never()).getTransactionsForAddress(any(), any());
    }

    @Test
    void shouldHandleEmptyTransactionList() {
        // Given
        WalletAddress wallet = new WalletAddress(walletAddress, ethereum, "Test Wallet");
        walletRepository.save(wallet).block();

        when(blockchainService.getTransactionsForAddress(walletAddress, ethereum))
            .thenReturn(Flux.empty());

        // When
        Flux<Transaction> result = useCase.trackWallet(walletAddress, ethereum);

        // Then
        StepVerifier.create(result)
            .verifyComplete();
    }

    private static int transactionCounter = 0;

    private Transaction createTestTransaction(Address from, Address to) {
        return createTestTransaction(from, to, TransactionValue.of(BigInteger.ONE), Instant.now());
    }

    private Transaction createTestTransaction(Address from, Address to, Instant timestamp) {
        return createTestTransaction(from, to, TransactionValue.of(BigInteger.ONE), timestamp);
    }

    private Transaction createTestTransaction(Address from, Address to, TransactionValue value, Instant timestamp) {
        // Generate unique hash with exactly 64 hex characters after 0x
        String baseHash = "9fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8b";
        String counterHex = String.format("%03x", transactionCounter++); // 3 hex digits for counter
        String uniqueHash = baseHash.substring(0, 61) + counterHex; // Keep exactly 64 chars total
        return new Transaction(
            TransactionHash.of("0x" + uniqueHash),
            from,
            to,
            ethereum,
            value,
            BigInteger.valueOf(20000000000L), // gas price
            BigInteger.valueOf(21000), // gas limit
            BigInteger.valueOf(21000), // gas used
            BigInteger.valueOf(18500000), // block number
            "0x8fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8a", // block hash
            0, // transaction index
            1, // status (success)
            timestamp,
            true // confirmed
        );
    }
}