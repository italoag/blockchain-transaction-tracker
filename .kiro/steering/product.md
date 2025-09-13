# Product Overview

This is a blockchain transaction tracking microservice built with Spring Boot WebFlux. The service monitors multiple blockchain networks (Ethereum, Polygon) in real-time and tracks transactions for specific addresses or wallets.

## Key Features

- Real-time blockchain transaction monitoring using WebSocket connections
- Multi-network support (Ethereum, Polygon, extensible to other networks)
- Bloom filter optimization to prevent duplicate transaction processing
- Bridge event detection for cross-chain transactions
- WebSocket API for real-time transaction streaming to clients
- Resilient architecture with circuit breakers and retry mechanisms

## Use Cases

- Wallet applications needing real-time transaction updates
- Cross-chain bridge monitoring and detection
- Transaction history tracking for specific addresses
- Real-time notifications for blockchain events