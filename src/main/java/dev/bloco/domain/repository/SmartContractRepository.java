package dev.bloco.domain.repository;

import dev.bloco.domain.entity.ContractEvent;
import dev.bloco.domain.entity.SmartContract;
import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Domain Repository interface for smart contract persistence.
 * Defines the contract for smart contract data access operations.
 */
public interface SmartContractRepository {

    /**
     * Save a smart contract
     */
    Mono<SmartContract> save(SmartContract contract);

    /**
     * Find contract by address and network
     */
    Mono<SmartContract> findByAddressAndNetwork(Address address, Network network);

    /**
     * Find all contracts for a network
     */
    Flux<SmartContract> findByNetwork(Network network);

    /**
     * Find contracts by creator address
     */
    Flux<SmartContract> findByCreator(Address creator);

    /**
     * Find contracts by type
     */
    Flux<SmartContract> findByType(SmartContract.ContractType type);

    /**
     * Find contracts deployed within time range
     */
    Flux<SmartContract> findByDeploymentTimeRange(Instant startTime, Instant endTime);

    /**
     * Check if contract exists
     */
    Mono<Boolean> existsByAddressAndNetwork(Address address, Network network);

    /**
     * Count contracts for a network
     */
    Mono<Long> countByNetwork(Network network);

    /**
     * Count contracts by type
     */
    Mono<Long> countByType(SmartContract.ContractType type);

    /**
     * Delete contract by address and network
     */
    Mono<Void> deleteByAddressAndNetwork(Address address, Network network);

    /**
     * Save contract event
     */
    Mono<ContractEvent> saveEvent(ContractEvent event);

    /**
     * Save multiple contract events
     */
    Flux<ContractEvent> saveEvents(Flux<ContractEvent> events);

    /**
     * Find events for a contract
     */
    Flux<ContractEvent> findEventsByContract(Address contractAddress, Network network);

    /**
     * Find events by event signature
     */
    Flux<ContractEvent> findEventsBySignature(String eventSignature, Network network);

    /**
     * Find events within time range
     */
    Flux<ContractEvent> findEventsByTimeRange(Instant startTime, Instant endTime, Network network);

    /**
     * Find recent events for a contract
     */
    Flux<ContractEvent> findRecentEventsByContract(Address contractAddress, Network network, int minutes);

    /**
     * Count events for a contract
     */
    Mono<Long> countEventsByContract(Address contractAddress, Network network);

    /**
     * Get contract statistics
     */
    Mono<ContractStats> getContractStats(Network network);

    /**
     * Contract statistics
     */
    class ContractStats {
        private final long totalContracts;
        private final long totalEvents;
        private final long erc20Contracts;
        private final long erc721Contracts;
        private final Instant oldestContract;
        private final Instant newestContract;

        public ContractStats(long totalContracts, long totalEvents, long erc20Contracts,
                           long erc721Contracts, Instant oldestContract, Instant newestContract) {
            this.totalContracts = totalContracts;
            this.totalEvents = totalEvents;
            this.erc20Contracts = erc20Contracts;
            this.erc721Contracts = erc721Contracts;
            this.oldestContract = oldestContract;
            this.newestContract = newestContract;
        }

        public long getTotalContracts() { return totalContracts; }
        public long getTotalEvents() { return totalEvents; }
        public long getErc20Contracts() { return erc20Contracts; }
        public long getErc721Contracts() { return erc721Contracts; }
        public Instant getOldestContract() { return oldestContract; }
        public Instant getNewestContract() { return newestContract; }
    }
}