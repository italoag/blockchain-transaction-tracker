package dev.bloco.presentation.controller;

import dev.bloco.application.usecase.GenerateAnalyticsUseCase;
import dev.bloco.application.usecase.MonitorBlocksUseCase;
import dev.bloco.application.usecase.MonitorContractEventsUseCase;
import dev.bloco.application.usecase.TrackWalletTransactionsUseCase;
import dev.bloco.domain.valueobject.Address;
import dev.bloco.domain.valueobject.Network;
import dev.bloco.presentation.dto.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

/**
 * REST Controller for blockchain monitoring operations.
 * Provides HTTP endpoints for tracking wallets, contracts, and blocks.
 */
@RestController
@RequestMapping("/api/v1/monitoring")
public class MonitoringController {

    private final TrackWalletTransactionsUseCase trackWalletUseCase;
    private final MonitorContractEventsUseCase monitorContractUseCase;
    private final MonitorBlocksUseCase monitorBlocksUseCase;
    private final GenerateAnalyticsUseCase analyticsUseCase;

    public MonitoringController(
            TrackWalletTransactionsUseCase trackWalletUseCase,
            MonitorContractEventsUseCase monitorContractUseCase,
            MonitorBlocksUseCase monitorBlocksUseCase,
            GenerateAnalyticsUseCase analyticsUseCase) {
        this.trackWalletUseCase = trackWalletUseCase;
        this.monitorContractUseCase = monitorContractUseCase;
        this.monitorBlocksUseCase = monitorBlocksUseCase;
        this.analyticsUseCase = analyticsUseCase;
    }

    /**
     * Track transactions for a specific wallet
     */
    @PostMapping("/wallets/{address}/track")
    public Flux<TransactionDto> trackWallet(
            @PathVariable String address,
            @RequestParam String network,
            @RequestParam(defaultValue = "60") int minutes) {

        Address walletAddress = Address.of(address);
        Network networkObj = getNetworkByName(network);

        return trackWalletUseCase.trackWallet(walletAddress, networkObj)
            .map(TransactionDto::fromDomain);
    }

    /**
     * Get recent transactions for a wallet
     */
    @GetMapping("/wallets/{address}/transactions")
    public Flux<TransactionDto> getWalletTransactions(
            @PathVariable String address,
            @RequestParam String network,
            @RequestParam(defaultValue = "60") int minutes) {

        Address walletAddress = Address.of(address);
        Network networkObj = getNetworkByName(network);

        return trackWalletUseCase.getRecentWalletTransactions(walletAddress, networkObj, minutes)
            .map(TransactionDto::fromDomain);
    }

    /**
     * Get wallet statistics
     */
    @GetMapping("/wallets/{address}/stats")
    public Mono<WalletStatsDto> getWalletStats(
            @PathVariable String address,
            @RequestParam String network) {

        Address walletAddress = Address.of(address);
        Network networkObj = getNetworkByName(network);

        return trackWalletUseCase.getWalletStats(walletAddress, networkObj)
            .map(WalletStatsDto::fromWalletTransactionStats);
    }

    /**
     * Monitor contract events
     */
    @PostMapping("/contracts/{address}/events")
    public Flux<ContractEventDto> monitorContract(
            @PathVariable String address,
            @RequestParam String network) {

        Address contractAddress = Address.of(address);
        Network networkObj = getNetworkByName(network);

        return monitorContractUseCase.monitorContract(contractAddress, networkObj)
            .map(ContractEventDto::fromDomain);
    }

    /**
     * Get recent contract events
     */
    @GetMapping("/contracts/{address}/events")
    public Flux<ContractEventDto> getContractEvents(
            @PathVariable String address,
            @RequestParam String network,
            @RequestParam(defaultValue = "60") int minutes) {

        Address contractAddress = Address.of(address);
        Network networkObj = getNetworkByName(network);

        return monitorContractUseCase.getRecentContractEvents(contractAddress, networkObj, minutes)
            .map(ContractEventDto::fromDomain);
    }

    /**
     * Get contract event statistics
     */
    @GetMapping("/contracts/{address}/events/stats")
    public Mono<ContractEventStatsDto> getContractEventStats(
            @PathVariable String address,
            @RequestParam String network) {

        Address contractAddress = Address.of(address);
        Network networkObj = getNetworkByName(network);

        return monitorContractUseCase.getContractEventStats(contractAddress, networkObj)
            .map(ContractEventStatsDto::fromDomain);
    }

    /**
     * Monitor new blocks
     */
    @GetMapping(value = "/blocks/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BlockDto> streamBlocks(@RequestParam String network) {
        Network networkObj = getNetworkByName(network);
        return monitorBlocksUseCase.monitorBlocks(networkObj)
            .map(BlockDto::fromDomain);
    }

    /**
     * Get latest block
     */
    @GetMapping("/blocks/latest")
    public Mono<BlockDto> getLatestBlock(@RequestParam String network) {
        Network networkObj = getNetworkByName(network);
        return monitorBlocksUseCase.getLatestBlock(networkObj)
            .map(BlockDto::fromDomain);
    }

    /**
     * Get blocks in range
     */
    @GetMapping("/blocks/range")
    public Flux<BlockDto> getBlocksInRange(
            @RequestParam String network,
            @RequestParam String startBlock,
            @RequestParam String endBlock) {

        Network networkObj = getNetworkByName(network);
        BigInteger start = new BigInteger(startBlock);
        BigInteger end = new BigInteger(endBlock);

        return monitorBlocksUseCase.getBlocksInRange(networkObj, start, end)
            .map(BlockDto::fromDomain);
    }

    /**
     * Get recent blocks
     */
    @GetMapping("/blocks/recent")
    public Flux<BlockDto> getRecentBlocks(
            @RequestParam String network,
            @RequestParam(defaultValue = "60") int minutes) {

        Network networkObj = getNetworkByName(network);
        return monitorBlocksUseCase.getRecentBlocks(networkObj, minutes)
            .map(BlockDto::fromDomain);
    }

    /**
     * Get network analytics
     */
    @GetMapping("/analytics/network")
    public Mono<NetworkAnalyticsDto> getNetworkAnalytics(@RequestParam String network) {
        Network networkObj = getNetworkByName(network);
        return analyticsUseCase.generateNetworkAnalytics(networkObj)
            .map(NetworkAnalyticsDto::fromDomain);
    }

    /**
     * Get system analytics
     */
    @GetMapping("/analytics/system")
    public Mono<SystemAnalyticsDto> getSystemAnalytics() {
        return analyticsUseCase.generateSystemAnalytics()
            .map(SystemAnalyticsDto::fromDomain);
    }

    /**
     * Get top active wallets
     */
    @GetMapping("/analytics/wallets/top")
    public Flux<WalletActivityDto> getTopActiveWallets(@RequestParam(defaultValue = "10") int limit) {
        return analyticsUseCase.getTopActiveWallets(limit)
            .map(WalletActivityDto::fromDomain);
    }

    /**
     * Get network by name
     */
    private Network getNetworkByName(String name) {
        return switch (name.toLowerCase()) {
            case "ethereum", "eth" -> Network.ethereum();
            case "polygon", "matic" -> Network.polygon();
            case "bsc", "binance" -> Network.bsc();
            default -> Network.of(name.toLowerCase(), name, 0); // Default with chainId 0
        };
    }
}