package dev.bloco.application.usecase;

import dev.bloco.domain.entity.ContractEvent;
import dev.bloco.domain.entity.SmartContract;
import dev.bloco.domain.repository.SmartContractRepository;
import dev.bloco.domain.service.BlockchainService;
import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Use Case for monitoring smart contract events.
 * Orchestrates the business logic for tracking contract activity.
 */
public class MonitorContractEventsUseCase {

    private final BlockchainService blockchainService;
    private final SmartContractRepository contractRepository;

    public MonitorContractEventsUseCase(
            BlockchainService blockchainService,
            SmartContractRepository contractRepository) {
        this.blockchainService = blockchainService;
        this.contractRepository = contractRepository;
    }

    /**
     * Monitor events for a specific contract
     */
    public Flux<ContractEvent> monitorContract(Address contractAddress, Network network) {
        return contractRepository.findByAddressAndNetwork(contractAddress, network)
            .flatMapMany(contract -> {
                if (!contract.isActive()) {
                    return Flux.empty();
                }
                return blockchainService.getContractEvents(contractAddress, network)
                    .filter(event -> contract.emitsEvent(event.getEventSignature()))
                    .flatMap(event -> contractRepository.saveEvent(event));
            });
    }

    /**
     * Monitor events for all active contracts
     */
    public Flux<ContractEvent> monitorAllActiveContracts() {
        return contractRepository.findByNetwork(null) // This would need to be implemented to get all contracts
            .filter(SmartContract::isActive)
            .flatMap(contract -> monitorContract(contract.getAddress(), contract.getNetwork()));
    }

    /**
     * Monitor events for contracts of a specific type
     */
    public Flux<ContractEvent> monitorContractsByType(SmartContract.ContractType contractType) {
        return contractRepository.findByType(contractType)
            .filter(SmartContract::isActive)
            .flatMap(contract -> monitorContract(contract.getAddress(), contract.getNetwork()));
    }

    /**
     * Get recent events for a contract
     */
    public Flux<ContractEvent> getRecentContractEvents(Address contractAddress, Network network, int minutes) {
        return contractRepository.findRecentEventsByContract(contractAddress, network, minutes);
    }

    /**
     * Get transfer events for a contract
     */
    public Flux<ContractEvent> getTransferEvents(Address contractAddress, Network network) {
        return contractRepository.findEventsByContract(contractAddress, network)
            .filter(ContractEvent::isTransferEvent);
    }

    /**
     * Get contract event statistics
     */
    public Mono<ContractEventStats> getContractEventStats(Address contractAddress, Network network) {
        return Mono.zip(
            contractRepository.countEventsByContract(contractAddress, network),
            contractRepository.findRecentEventsByContract(contractAddress, network, 60).count(),
            contractRepository.findEventsByContract(contractAddress, network)
                .filter(ContractEvent::isTransferEvent)
                .count()
        ).map(tuple -> new ContractEventStats(
            tuple.getT1(),
            tuple.getT2(),
            tuple.getT3()
        ));
    }

    /**
     * Contract event statistics
     */
    public static class ContractEventStats {
        private final long totalEvents;
        private final long recentEvents;
        private final long transferEvents;

        public ContractEventStats(long totalEvents, long recentEvents, long transferEvents) {
            this.totalEvents = totalEvents;
            this.recentEvents = recentEvents;
            this.transferEvents = transferEvents;
        }

        public long getTotalEvents() { return totalEvents; }
        public long getRecentEvents() { return recentEvents; }
        public long getTransferEvents() { return transferEvents; }
    }
}