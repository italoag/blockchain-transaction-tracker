package dev.bloco.domain.entity;

import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Rich Domain Entity representing a smart contract to monitor.
 * Follows Rich Domain Model pattern with business logic encapsulated.
 */
public class SmartContract {
    /**
     * Contract types for categorization
     */
    public enum ContractType {
        ERC20("ERC-20 Token"),
        ERC721("ERC-721 NFT"),
        ERC1155("ERC-1155 Multi-Token"),
        DEFI("DeFi Protocol"),
        BRIDGE("Cross-chain Bridge"),
        GOVERNANCE("Governance"),
        STAKING("Staking Contract"),
        DEX("Decentralized Exchange"),
        LENDING("Lending Protocol"),
        UNKNOWN("Unknown");

        private final String displayName;

        ContractType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final Address address;
    private final Network network;
    private final String name;
    private final String abi;
    private final List<String> eventsToMonitor;
    private final Instant deployedAt;
    private final ContractType contractType;
    private boolean active;

    public SmartContract(Address address, Network network, String name, String abi, List<String> eventsToMonitor) {
        this(address, network, name, abi, eventsToMonitor, ContractType.UNKNOWN);
    }

    public SmartContract(Address address, Network network, String name, String abi, List<String> eventsToMonitor, ContractType contractType) {
        this.address = address;
        this.network = network;
        this.name = name != null ? name : "";
        this.abi = abi != null ? abi : "";
        this.eventsToMonitor = eventsToMonitor != null ? List.copyOf(eventsToMonitor) : List.of();
        this.contractType = contractType != null ? contractType : ContractType.UNKNOWN;
        this.deployedAt = Instant.now();
        this.active = true;
    }

    public SmartContract(Address address, Network network) {
        this(address, network, null, null, null, ContractType.UNKNOWN);
    }

    /**
     * Business method: Check if contract emits a specific event
     */
    public boolean emitsEvent(String eventSignature) {
        return eventsToMonitor.contains(eventSignature);
    }

    /**
     * Business method: Check if contract should monitor all events
     */
    public boolean monitorsAllEvents() {
        return eventsToMonitor.isEmpty();
    }

    /**
     * Business method: Add event to monitor
     */
    public SmartContract addEventToMonitor(String eventSignature) {
        if (eventSignature == null || eventSignature.trim().isEmpty()) {
            throw new IllegalArgumentException("Event signature cannot be null or empty");
        }
        List<String> newEvents = new java.util.ArrayList<>(eventsToMonitor);
        if (!newEvents.contains(eventSignature)) {
            newEvents.add(eventSignature);
        }
        return new SmartContract(address, network, name, abi, newEvents, contractType);
    }

    /**
     * Business method: Remove event from monitoring
     */
    public SmartContract removeEventToMonitor(String eventSignature) {
        List<String> newEvents = eventsToMonitor.stream()
                .filter(event -> !event.equals(eventSignature))
                .toList();
        return new SmartContract(address, network, name, abi, newEvents, contractType);
    }

    /**
     * Business method: Check if contract is involved in a transaction
     */
    public boolean isInvolvedIn(Transaction transaction) {
        return transaction.involvesAddress(address) && transaction.getNetwork().equals(network);
    }

    /**
     * Business method: Deactivate monitoring for this contract
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Business method: Reactivate monitoring for this contract
     */
    public void reactivate() {
        this.active = true;
    }

    /**
     * Business method: Get contract age in days
     */
    public long getAgeInDays() {
        long ageSeconds = Instant.now().getEpochSecond() - deployedAt.getEpochSecond();
        return ageSeconds / (24 * 60 * 60);
    }

    /**
     * Business method: Check if contract has ABI available
     */
    public boolean hasAbi() {
        return abi != null && !abi.trim().isEmpty();
    }

    /**
     * Business method: Get display name for the contract
     */
    public String getDisplayName() {
        if (!name.isEmpty()) {
            return name;
        }
        return address.toString();
    }

    // Getters
    public Address getAddress() { return address; }
    public Network getNetwork() { return network; }
    public String getName() { return name; }
    public String getAbi() { return abi; }
    public List<String> getEventsToMonitor() { return eventsToMonitor; }
    public Instant getDeployedAt() { return deployedAt; }
    public ContractType getContractType() { return contractType; }
    public boolean isActive() { return active; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SmartContract that = (SmartContract) o;
        return address.equals(that.address) && network.equals(that.network);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, network);
    }

    @Override
    public String toString() {
        return String.format("SmartContract{address=%s, network=%s, name='%s', active=%s}",
                           address, network, name, active);
    }
}