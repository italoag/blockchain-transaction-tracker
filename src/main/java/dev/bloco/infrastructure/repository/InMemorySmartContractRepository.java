package dev.bloco.infrastructure.repository;

import dev.bloco.domain.entity.ContractEvent;
import dev.bloco.domain.entity.SmartContract;
import dev.bloco.domain.repository.SmartContractRepository;
import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory implementation of SmartContractRepository.
 * Thread-safe implementation using ConcurrentHashMap for storage.
 */
public class InMemorySmartContractRepository implements SmartContractRepository {

    private final ConcurrentMap<String, SmartContract> contractStorage = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CopyOnWriteArrayList<ContractEvent>> eventStorage = new ConcurrentHashMap<>();

    @Override
    public Mono<SmartContract> save(SmartContract smartContract) {
        return Mono.fromCallable(() -> {
            String key = createKey(smartContract.getAddress(), smartContract.getNetwork());
            contractStorage.put(key, smartContract);
            return smartContract;
        });
    }

    @Override
    public Mono<SmartContract> findByAddressAndNetwork(Address address, Network network) {
        return Mono.fromCallable(() -> {
            String key = createKey(address, network);
            return contractStorage.get(key);
        });
    }

    @Override
    public Flux<SmartContract> findByNetwork(Network network) {
        return Flux.fromIterable(contractStorage.values())
            .filter(contract -> contract.getNetwork().equals(network));
    }

    @Override
    public Flux<SmartContract> findByCreator(Address creator) {
        return Flux.fromIterable(contractStorage.values())
            .filter(contract -> contract.getAddress().equals(creator));
    }

    @Override
    public Flux<SmartContract> findByType(SmartContract.ContractType type) {
        return Flux.fromIterable(contractStorage.values())
            .filter(contract -> contract.getContractType().equals(type));
    }

    @Override
    public Flux<SmartContract> findByDeploymentTimeRange(Instant startTime, Instant endTime) {
        return Flux.fromIterable(contractStorage.values())
            .filter(contract -> {
                Instant deployedAt = contract.getDeployedAt();
                return deployedAt.isAfter(startTime) && deployedAt.isBefore(endTime);
            });
    }

    @Override
    public Mono<Boolean> existsByAddressAndNetwork(Address address, Network network) {
        return Mono.fromCallable(() -> {
            String key = createKey(address, network);
            return contractStorage.containsKey(key);
        });
    }

    @Override
    public Mono<Long> countByNetwork(Network network) {
        return Flux.fromIterable(contractStorage.values())
            .filter(contract -> contract.getNetwork().equals(network))
            .count();
    }

    @Override
    public Mono<Long> countByType(SmartContract.ContractType type) {
        return Flux.fromIterable(contractStorage.values())
            .filter(contract -> contract.getContractType().equals(type))
            .count();
    }

    @Override
    public Mono<Void> deleteByAddressAndNetwork(Address address, Network network) {
        return Mono.fromRunnable(() -> {
            String key = createKey(address, network);
            contractStorage.remove(key);
            eventStorage.remove(key);
        });
    }

    @Override
    public Mono<ContractEvent> saveEvent(ContractEvent event) {
        return Mono.fromCallable(() -> {
            String key = createKey(event.getContractAddress(), event.getNetwork());
            eventStorage.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(event);
            return event;
        });
    }

    @Override
    public Flux<ContractEvent> saveEvents(Flux<ContractEvent> events) {
        return events.flatMap(this::saveEvent);
    }

    @Override
    public Flux<ContractEvent> findEventsByContract(Address contractAddress, Network network) {
        return Mono.fromCallable(() -> {
            String key = createKey(contractAddress, network);
            return eventStorage.getOrDefault(key, new CopyOnWriteArrayList<>());
        }).flatMapMany(Flux::fromIterable);
    }

    @Override
    public Flux<ContractEvent> findEventsBySignature(String eventSignature, Network network) {
        return Flux.fromIterable(eventStorage.values())
            .flatMap(Flux::fromIterable)
            .filter(event -> event.getNetwork().equals(network))
            .filter(event -> event.getEventSignature().equals(eventSignature));
    }

    @Override
    public Flux<ContractEvent> findEventsByTimeRange(Instant startTime, Instant endTime, Network network) {
        return Flux.fromIterable(eventStorage.values())
            .flatMap(Flux::fromIterable)
            .filter(event -> event.getNetwork().equals(network))
            .filter(event -> {
                Instant timestamp = event.getTimestamp();
                return timestamp.isAfter(startTime) && timestamp.isBefore(endTime);
            });
    }

    @Override
    public Flux<ContractEvent> findRecentEventsByContract(Address contractAddress, Network network, int minutes) {
        Instant cutoffTime = Instant.now().minusSeconds(minutes * 60L);
        return findEventsByContract(contractAddress, network)
            .filter(event -> event.getTimestamp().isAfter(cutoffTime));
    }

    @Override
    public Mono<Long> countEventsByContract(Address contractAddress, Network network) {
        return findEventsByContract(contractAddress, network).count();
    }

    @Override
    public Mono<ContractStats> getContractStats(Network network) {
        return Flux.fromIterable(contractStorage.values())
            .filter(contract -> contract.getNetwork().equals(network))
            .collectList()
            .zipWith(
                Flux.fromIterable(eventStorage.values())
                    .flatMap(Flux::fromIterable)
                    .filter(event -> event.getNetwork().equals(network))
                    .count()
            )
            .map(tuple -> {
                var contracts = tuple.getT1();
                long totalEvents = tuple.getT2();

                long erc20Contracts = contracts.stream()
                    .mapToLong(contract -> contract.getContractType().equals(SmartContract.ContractType.ERC20) ? 1 : 0)
                    .sum();
                long erc721Contracts = contracts.stream()
                    .mapToLong(contract -> contract.getContractType().equals(SmartContract.ContractType.ERC721) ? 1 : 0)
                    .sum();

                Instant oldestContract = contracts.stream()
                    .map(SmartContract::getDeployedAt)
                    .min(Instant::compareTo)
                    .orElse(Instant.now());

                Instant newestContract = contracts.stream()
                    .map(SmartContract::getDeployedAt)
                    .max(Instant::compareTo)
                    .orElse(Instant.now());

                return new ContractStats(contracts.size(), totalEvents, erc20Contracts,
                                       erc721Contracts, oldestContract, newestContract);
            });
    }

    private String createKey(Address address, Network network) {
        return network.getName() + ":" + address.getValue();
    }
}
