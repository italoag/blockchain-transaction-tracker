package dev.bloco.domain.service;

import dev.bloco.domain.entity.Transaction;
import dev.bloco.domain.entity.WalletAddress;
import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Domain Service interface for transaction tracking operations.
 * Defines the contract for transaction-related business operations.
 */
public interface TransactionTrackingService {

    /**
     * Track transactions for a specific wallet address
     */
    Flux<Transaction> trackWallet(WalletAddress wallet);

    /**
     * Track transactions for a specific address on a network
     */
    Flux<Transaction> trackAddress(Address address, Network network);

    /**
     * Track transactions across multiple networks for an address
     */
    Flux<Transaction> trackAddressAcrossNetworks(Address address, java.util.List<Network> networks);

    /**
     * Get transaction by hash
     */
    Mono<Transaction> getTransactionByHash(dev.bloco.domain.valueobject.TransactionHash hash, Network network);
}