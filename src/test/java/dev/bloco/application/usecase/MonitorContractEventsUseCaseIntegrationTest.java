package dev.bloco.application.usecase;

import dev.bloco.domain.entity.ContractEvent;
import dev.bloco.domain.entity.SmartContract;
import dev.bloco.domain.service.BlockchainService;
import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import dev.bloco.domain.valueobject.TransactionHash;
import dev.bloco.infrastructure.repository.InMemorySmartContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for MonitorContractEventsUseCase.
 * Tests the complete workflow with in-memory repositories.
 */
class MonitorContractEventsUseCaseIntegrationTest {

    private MonitorContractEventsUseCase useCase;
    private InMemorySmartContractRepository contractRepository;
    private BlockchainService blockchainService;

    private final Network ethereum = Network.of("ethereum", "Ethereum Mainnet", 1);
    private final Address contractAddress = Address.of("0x742d35Cc6634C0532925a3b844Bc454e4438f44e");
    private final Address otherContractAddress = Address.of("0x8ba1f109551bD432803012645261713D3e8f6c41");
    private static int eventCounter = 0;

    @BeforeEach
    void setUp() {
        contractRepository = new InMemorySmartContractRepository();
        blockchainService = mock(BlockchainService.class);
        useCase = new MonitorContractEventsUseCase(blockchainService, contractRepository);
    }

    @Test
    void shouldMonitorContractEventsSuccessfully() {
        // Given
        SmartContract contract = new SmartContract(
            contractAddress,
            ethereum,
            "Test Contract",
            "[]",
            Arrays.asList("Transfer(address,address,uint256)", "Approval(address,address,uint256)")
        );
        contractRepository.save(contract).block();

        ContractEvent event = createTestContractEvent(contractAddress, "Transfer(address,address,uint256)");
        when(blockchainService.getContractEvents(contractAddress, ethereum))
            .thenReturn(Flux.just(event));

        // When
        Flux<ContractEvent> result = useCase.monitorContract(contractAddress, ethereum);

        // Then
        StepVerifier.create(result)
            .expectNext(event)
            .verifyComplete();

        verify(blockchainService).getContractEvents(contractAddress, ethereum);
    }

    @Test
    void shouldSkipInactiveContract() {
        // Given
        SmartContract contract = new SmartContract(
            contractAddress,
            ethereum,
            "Test Contract",
            "[]",
            Arrays.asList("Transfer(address,address,uint256)")
        );
        contract.deactivate();
        contractRepository.save(contract).block();

        // When
        Flux<ContractEvent> result = useCase.monitorContract(contractAddress, ethereum);

        // Then
        StepVerifier.create(result)
            .verifyComplete();

        verify(blockchainService, never()).getContractEvents(any(), any());
    }

    @Test
    void shouldFilterEventsByContractConfiguration() {
        // Given
        SmartContract contract = new SmartContract(
            contractAddress,
            ethereum,
            "Test Contract",
            "[]",
            Arrays.asList("Transfer(address,address,uint256)") // Only monitor Transfer events
        );
        contractRepository.save(contract).block();

        ContractEvent transferEvent = createTestContractEvent(contractAddress, "Transfer(address,address,uint256)");
        ContractEvent approvalEvent = createTestContractEvent(contractAddress, "Approval(address,address,uint256)");

        when(blockchainService.getContractEvents(contractAddress, ethereum))
            .thenReturn(Flux.just(transferEvent, approvalEvent));

        // When
        Flux<ContractEvent> result = useCase.monitorContract(contractAddress, ethereum);

        // Then
        StepVerifier.create(result)
            .expectNext(transferEvent)
            .verifyComplete();
    }

    @Test
    void shouldMonitorContractsByType() {
        // Given
        SmartContract erc20Contract = new SmartContract(
            contractAddress,
            ethereum,
            "ERC20 Token",
            "[]",
            Arrays.asList("Transfer(address,address,uint256)"),
            SmartContract.ContractType.ERC20
        );
        SmartContract erc721Contract = new SmartContract(
            otherContractAddress,
            ethereum,
            "ERC721 Token",
            "[]",
            Arrays.asList("Transfer(address,address,uint256)"),
            SmartContract.ContractType.ERC721
        );

        contractRepository.save(erc20Contract).block();
        contractRepository.save(erc721Contract).block();

        ContractEvent erc20Event = createTestContractEvent(contractAddress, "Transfer(address,address,uint256)");
        ContractEvent erc721Event = createTestContractEvent(otherContractAddress, "Transfer(address,address,uint256)");

        when(blockchainService.getContractEvents(contractAddress, ethereum))
            .thenReturn(Flux.just(erc20Event));
        when(blockchainService.getContractEvents(otherContractAddress, ethereum))
            .thenReturn(Flux.just(erc721Event));

        // When
        Flux<ContractEvent> result = useCase.monitorContractsByType(SmartContract.ContractType.ERC20);

        // Then
        StepVerifier.create(result)
            .expectNext(erc20Event)
            .verifyComplete();
    }

    @Test
    void shouldGetRecentContractEvents() {
        // Given
        ContractEvent recentEvent = createTestContractEvent(contractAddress, "Transfer(address,address,uint256)");
        ContractEvent oldEvent = createTestContractEventWithTimestamp(contractAddress, "Transfer(address,address,uint256)",
            Instant.now().minusSeconds(7200)); // 2 hours ago

        contractRepository.saveEvent(recentEvent).block();
        contractRepository.saveEvent(oldEvent).block();

        // When
        Flux<ContractEvent> result = useCase.getRecentContractEvents(contractAddress, ethereum, 60);

        // Then
        StepVerifier.create(result)
            .expectNext(recentEvent)
            .verifyComplete();
    }

    @Test
    void shouldGetTransferEvents() {
        // Given
        ContractEvent transferEvent = createTestContractEvent(contractAddress, "Transfer(address,address,uint256)");
        ContractEvent approvalEvent = createTestContractEventWithApprovalSignature(contractAddress, "Approval(address,address,uint256)");

        contractRepository.saveEvent(transferEvent).block();
        contractRepository.saveEvent(approvalEvent).block();

        // When
        Flux<ContractEvent> result = useCase.getTransferEvents(contractAddress, ethereum);

        // Then
        StepVerifier.create(result)
            .expectNext(transferEvent)
            .verifyComplete();
    }

    @Test
    void shouldGetContractEventStats() {
        // Given
        ContractEvent transferEvent1 = createTestContractEventWithTimestamp(contractAddress, "Transfer(address,address,uint256)",
            Instant.now().minusSeconds(7200)); // 2 hours ago
        ContractEvent transferEvent2 = createTestContractEventWithTimestamp(contractAddress, "Transfer(address,address,uint256)",
            Instant.now().minusSeconds(3600)); // 1 hour ago
        ContractEvent approvalEvent = createTestContractEventWithApprovalSignature(contractAddress, "Approval(address,address,uint256)",
            Instant.now().minusSeconds(7200)); // 2 hours ago
        ContractEvent recentTransferEvent = createTestContractEvent(contractAddress, "Transfer(address,address,uint256)"); // now

        contractRepository.saveEvents(Flux.just(transferEvent1, transferEvent2, approvalEvent)).blockLast();
        contractRepository.saveEvent(recentTransferEvent).block();

        // When
        Mono<MonitorContractEventsUseCase.ContractEventStats> result =
            useCase.getContractEventStats(contractAddress, ethereum);

        // Then
        StepVerifier.create(result)
            .assertNext(stats -> {
                assert stats.getTotalEvents() == 4 : "Expected 4 total events, got " + stats.getTotalEvents();
                assert stats.getRecentEvents() == 1 : "Expected 1 recent event, got " + stats.getRecentEvents();
                assert stats.getTransferEvents() == 3 : "Expected 3 transfer events, got " + stats.getTransferEvents();
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleContractNotFound() {
        // When
        Flux<ContractEvent> result = useCase.monitorContract(contractAddress, ethereum);

        // Then
        StepVerifier.create(result)
            .verifyComplete();

        verify(blockchainService, never()).getContractEvents(any(), any());
    }

    @Test
    void shouldHandleEmptyEventList() {
        // Given
        SmartContract contract = new SmartContract(
            contractAddress,
            ethereum,
            "Test Contract",
            "[]",
            Arrays.asList("Transfer(address,address,uint256)")
        );
        contractRepository.save(contract).block();

        when(blockchainService.getContractEvents(contractAddress, ethereum))
            .thenReturn(Flux.empty());

        // When
        Flux<ContractEvent> result = useCase.monitorContract(contractAddress, ethereum);

        // Then
        StepVerifier.create(result)
            .verifyComplete();
    }

    @Test
    void shouldHandleBlockchainServiceError() {
        // Given
        SmartContract contract = new SmartContract(
            contractAddress,
            ethereum,
            "Test Contract",
            "[]",
            Arrays.asList("Transfer(address,address,uint256)")
        );
        contractRepository.save(contract).block();

        when(blockchainService.getContractEvents(contractAddress, ethereum))
            .thenReturn(Flux.error(new RuntimeException("Blockchain service error")));

        // When
        Flux<ContractEvent> result = useCase.monitorContract(contractAddress, ethereum);

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();
    }

    private ContractEvent createTestContractEvent(Address contractAddress, String eventSignature) {
        return createTestContractEvent(contractAddress, eventSignature, Instant.now());
    }

    private ContractEvent createTestContractEvent(Address contractAddress, String eventSignature, Instant timestamp) {
        // Generate unique hash with exactly 64 hex characters after 0x
        String baseHash = "9fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8b";
        String counterHex = String.format("%03x", eventCounter++); // 3 hex digits for counter
        String uniqueHash = baseHash.substring(0, 61) + counterHex; // Keep exactly 64 chars total

        List<String> topics = Arrays.asList(
            "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef", // Transfer signature
            "0x000000000000000000000000742d35cc6634c0532925a3b844bc454e4438f44e", // from
            "0x0000000000000000000000008ba1f109551bd432803012645261713d3e8f6c41"  // to
        );

        return new ContractEvent(
            TransactionHash.of("0x" + uniqueHash),
            contractAddress,
            eventSignature,
            topics,
            "0x0000000000000000000000000000000000000000000000000de0b6b3a7640000", // 1 ETH in wei
            timestamp,
            ethereum,
            0,
            false
        );
    }

    private ContractEvent createTestContractEventWithTimestamp(Address contractAddress, String eventSignature, Instant timestamp) {
        return createTestContractEvent(contractAddress, eventSignature, timestamp);
    }

    private ContractEvent createTestContractEventWithApprovalSignature(Address contractAddress, String eventSignature) {
        return createTestContractEventWithApprovalSignature(contractAddress, eventSignature, Instant.now());
    }

    private ContractEvent createTestContractEventWithApprovalSignature(Address contractAddress, String eventSignature, Instant timestamp) {
        // Generate unique hash with exactly 64 hex characters after 0x
        String baseHash = "9fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8b";
        String counterHex = String.format("%03x", eventCounter++); // 3 hex digits for counter
        String uniqueHash = baseHash.substring(0, 61) + counterHex; // Keep exactly 64 chars total

        List<String> topics = Arrays.asList(
            "0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925", // Approval signature
            "0x000000000000000000000000742d35cc6634c0532925a3b844bc454e4438f44e", // owner
            "0x0000000000000000000000008ba1f109551bd432803012645261713d3e8f6c41"  // spender
        );

        return new ContractEvent(
            TransactionHash.of("0x" + uniqueHash),
            contractAddress,
            eventSignature,
            topics,
            "0x0000000000000000000000000000000000000000000000000de0b6b3a7640000", // 1 ETH in wei
            timestamp,
            ethereum,
            0,
            false
        );
    }
}