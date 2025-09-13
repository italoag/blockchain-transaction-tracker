# Data Model: Blockchain Monitoring Service

**Feature**: 002-com-base-nas
**Date**: September 12, 2025

## Overview
This document defines the core data entities for the blockchain monitoring service. All entities use Java records for immutability and thread-safety.

## Core Entities

### 1. BlockchainNetwork
Represents a blockchain network configuration and connection details.

**Fields**:
- `String id` - Unique network identifier (e.g., "ethereum", "polygon")
- `String name` - Human-readable network name
- `String wsUrl` - WebSocket endpoint URL (optional)
- `String httpUrl` - HTTP JSON-RPC endpoint URL
- `boolean supportsWebSocket` - Whether WebSocket connections are supported
- `boolean supportsHttp` - Whether HTTP connections are supported

**Validation Rules**:
- `id` must be non-null and non-empty
- At least one of `wsUrl` or `httpUrl` must be provided
- URLs must be valid HTTP/HTTPS/WSS protocols

**Relationships**:
- 1:N with Transaction (one network, many transactions)
- 1:N with WalletAddress (one network, many addresses)
- 1:N with SmartContract (one network, many contracts)

### 2. WalletAddress
Represents a wallet address to monitor for transactions.

**Fields**:
- `String address` - The wallet address (hex format)
- `String networkId` - Reference to BlockchainNetwork.id
- `String label` - Optional human-readable label
- `Instant createdAt` - When monitoring started
- `boolean active` - Whether monitoring is active

**Validation Rules**:
- `address` must be valid hex format (0x prefix, 40 characters)
- `networkId` must reference existing BlockchainNetwork
- `createdAt` must be non-null

**Relationships**:
- N:1 with BlockchainNetwork
- 1:N with Transaction (as sender or receiver)

### 3. SmartContract
Represents a smart contract to monitor for events and interactions.

**Fields**:
- `String address` - Contract address (hex format)
- `String networkId` - Reference to BlockchainNetwork.id
- `String name` - Optional contract name
- `String abi` - Contract ABI JSON (optional)
- `List<String> eventsToMonitor` - Event signatures to track
- `Instant deployedAt` - Contract deployment timestamp
- `boolean active` - Whether monitoring is active

**Validation Rules**:
- `address` must be valid hex format (0x prefix, 40 characters)
- `networkId` must reference existing BlockchainNetwork
- `eventsToMonitor` can be empty (monitor all events)

**Relationships**:
- N:1 with BlockchainNetwork
- 1:N with ContractEvent
- 1:N with Transaction (contract interactions)

### 4. Transaction
Represents a blockchain transaction with all relevant metadata.

**Fields**:
- `String hash` - Transaction hash (unique identifier)
- `String networkId` - Reference to BlockchainNetwork.id
- `String from` - Sender address
- `String to` - Receiver address (null for contract creation)
- `String value` - Transaction value in wei
- `String gasPrice` - Gas price in wei
- `String gasLimit` - Gas limit
- `String gasUsed` - Actual gas used (after mining)
- `BigInteger blockNumber` - Block number containing transaction
- `String blockHash` - Block hash
- `int transactionIndex` - Index within block
- `int status` - Transaction status (1 = success, 0 = failure)
- `Instant timestamp` - Block timestamp
- `boolean confirmed` - Whether transaction has sufficient confirmations
- `List<ContractEvent> events` - Associated contract events

**Validation Rules**:
- `hash` must be valid hex format (0x prefix, 64 characters)
- `networkId` must reference existing BlockchainNetwork
- `from` and `to` must be valid addresses when present
- `blockNumber` must be positive when confirmed

**Relationships**:
- N:1 with BlockchainNetwork
- N:1 with WalletAddress (via from/to addresses)
- N:1 with SmartContract (when to address is contract)
- 1:N with ContractEvent

### 5. ContractEvent
Represents an event emitted by a smart contract.

**Fields**:
- `String transactionHash` - Reference to Transaction.hash
- `String contractAddress` - Contract that emitted the event
- `String eventSignature` - Event signature (e.g., "Transfer(address,address,uint256)")
- `List<String> topics` - Indexed event parameters
- `String data` - Non-indexed event data
- `int logIndex` - Index within transaction logs
- `BigInteger blockNumber` - Block number
- `Instant timestamp` - Block timestamp

**Validation Rules**:
- `transactionHash` must reference existing Transaction
- `contractAddress` must be valid address
- `eventSignature` must be valid Solidity event signature

**Relationships**:
- N:1 with Transaction
- N:1 with SmartContract

## Data Flow Patterns

### Transaction Monitoring Flow
1. **Input**: TrackingRequest with wallet/contract addresses and networks
2. **Processing**: Addresses mapped to WalletAddress/SmartContract entities
3. **Monitoring**: Web3j clients stream transactions from specified networks
4. **Filtering**: Transactions filtered by address matches
5. **Deduplication**: Bloom filter prevents duplicate processing
6. **Storage**: Valid transactions stored with metadata
7. **Output**: Real-time streaming to clients

### Event Monitoring Flow
1. **Input**: SmartContract with eventsToMonitor list
2. **Processing**: Event signatures converted to topic filters
3. **Monitoring**: Web3j log filters capture contract events
4. **Enrichment**: Events linked to parent transactions
5. **Storage**: ContractEvent entities created and stored
6. **Output**: Events streamed with transaction context

## Persistence Strategy

### In-Memory Repository Pattern
- **TransactionRepository**: Interface for transaction persistence
- **InMemoryTransactionRepository**: Thread-safe implementation using CopyOnWriteArrayList
- **Reactive API**: All operations return Flux/Mono for reactive composition

### Future Persistence Options
- **Database**: PostgreSQL with reactive drivers (R2DBC)
- **Time-Series**: InfluxDB for transaction metrics
- **Search**: Elasticsearch for transaction/event querying
- **Cache**: Redis for frequently accessed data

## Validation & Business Rules

### Address Validation
- Ethereum address format: 0x + 40 hexadecimal characters
- Checksum validation using EIP-55 when possible
- Network-specific address formats (e.g., Polygon same as Ethereum)

### Transaction Validation
- Hash uniqueness within network
- Address validity for from/to fields
- Value and gas field ranges
- Block number and timestamp consistency

### Event Validation
- Topic hash validation against known event signatures
- Data field decoding based on ABI
- Log index uniqueness within transaction

## Performance Considerations

### Memory Usage
- Bloom filter: 16M bits (~2MB) for transaction deduplication
- Entity storage: Bounded collections to prevent memory leaks
- Connection pooling: Reused Web3j clients

### Processing Efficiency
- Reactive streams prevent blocking operations
- Parallel processing for multiple networks
- Efficient filtering reduces processing overhead

### Scalability Limits
- Maximum monitored addresses: Configurable (default: 10,000)
- Maximum concurrent networks: Based on available connections
- Transaction retention: Time-based or count-based cleanup

## Error Handling

### Validation Errors
- Invalid addresses: Return descriptive error messages
- Network unavailability: Circuit breaker with fallback
- Rate limiting: Exponential backoff retry

### Data Consistency
- Transaction confirmation: Wait for block confirmations
- Event ordering: Maintain log index sequence
- Duplicate prevention: Bloom filter with low false positive rate

## Migration Strategy

### From Current Implementation
1. **Data Migration**: Existing in-memory data to new entity structure
2. **API Compatibility**: Maintain existing endpoint contracts
3. **Gradual Rollout**: Feature flags for new functionality

### Future Enhancements
1. **Database Integration**: Migrate from in-memory to persistent storage
2. **Indexing**: Add database indexes for query performance
3. **Caching**: Implement Redis for hot data paths