# Quick Start Guide: Blockchain Monitoring Service

**Feature**: 002-com-base-nas
**Date**: September 12, 2025

## Overview
This guide provides step-by-step instructions for setting up and testing the blockchain monitoring service. The service monitors Ethereum-compatible networks for wallet transactions, contract interactions, and events.

## Prerequisites

### System Requirements
- Java 21 (Eclipse Temurin recommended)
- Maven 3.8+
- Docker (optional, for containerized deployment)

### Environment Variables
Set the following environment variables for blockchain provider access:

```bash
# Ethereum Mainnet
export ETHEREUM_WS_URL=wss://mainnet.infura.io/ws/v3/YOUR_INFURA_KEY
export ETHEREUM_HTTP_URL=https://mainnet.infura.io/v3/YOUR_INFURA_KEY

# Polygon Mainnet
export POLYGON_WS_URL=wss://polygon-mainnet.infura.io/ws/v3/YOUR_INFURA_KEY
export POLYGON_HTTP_URL=https://polygon-mainnet.infura.io/v3/YOUR_INFURA_KEY
```

## Quick Start Scenarios

### Scenario 1: Monitor Vitalik's Wallet (Ethereum)

**Objective**: Monitor transactions for a specific wallet address on Ethereum.

**Steps**:
1. **Start the service**:
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Send monitoring request**:
   ```bash
   curl -X POST http://localhost:8080/tracking/transactions \
     -H "Content-Type: application/json" \
     -d '{
       "type": "wallet",
       "value": "0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045",
       "networks": ["ethereum"]
     }'
   ```

3. **Expected Response**:
   ```json
   {
     "streamId": "stream-123",
     "status": "active",
     "message": "Monitoring started for wallet 0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045 on ethereum"
   }
   ```

4. **Connect via WebSocket** (optional):
   ```javascript
   const ws = new WebSocket('ws://localhost:8080/ws/tracking?type=wallet&value=0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045&networks=ethereum');

   ws.onmessage = (event) => {
     const transaction = JSON.parse(event.data);
     console.log('New transaction:', transaction);
   };
   ```

**Success Criteria**:
- Service responds with HTTP 200
- WebSocket connection receives transaction data
- Transactions include valid hash, from/to addresses, and network metadata

### Scenario 2: Monitor USDT Contract Events (Multi-Network)

**Objective**: Monitor transfer events for the USDT contract on both Ethereum and Polygon.

**Steps**:
1. **Start the service** (if not already running)

2. **Send monitoring request**:
   ```bash
   curl -X POST http://localhost:8080/tracking/transactions \
     -H "Content-Type: application/json" \
     -d '{
       "type": "contract",
       "value": "0xdAC17F958D2ee523a2206206994597C13D831ec7",
       "networks": ["ethereum", "polygon"]
     }'
   ```

3. **Monitor via WebSocket**:
   ```javascript
   const ws = new WebSocket('ws://localhost:8080/ws/tracking?type=contract&value=0xdAC17F958D2ee523a2206206994597C13D831ec7&networks=ethereum,polygon');

   ws.onmessage = (event) => {
     const eventData = JSON.parse(event.data);
     console.log('Contract event:', eventData);
   };
   ```

**Success Criteria**:
- Service monitors contract on both networks
- Transfer events are captured and streamed
- Event data includes transaction hash, topics, and decoded parameters

### Scenario 3: Check Service Status

**Objective**: Verify service health and monitoring statistics.

**Steps**:
1. **Check monitoring status**:
   ```bash
   curl http://localhost:8080/tracking/status
   ```

2. **Expected Response**:
   ```json
   {
     "activeStreams": 2,
     "totalTransactions": 150,
     "networks": [
       {
         "id": "ethereum",
         "name": "Ethereum",
         "status": "connected",
         "lastBlock": "18500000",
         "activeConnections": 1
       }
     ],
     "uptime": "00:15:30",
     "lastBlock": {
       "ethereum": "18500000",
       "polygon": "45000000"
     }
   }
   ```

**Success Criteria**:
- Service responds with HTTP 200
- Status shows active connections and recent block numbers
- Uptime indicates service stability

## Docker Deployment

### Build and Run with Docker

1. **Build the image**:
   ```bash
   docker build -t blockchain-tracker .
   ```

2. **Run with environment variables**:
   ```bash
   docker run -p 8080:8080 \
     -e ETHEREUM_WS_URL=wss://mainnet.infura.io/ws/v3/YOUR_KEY \
     -e POLYGON_WS_URL=wss://polygon-mainnet.infura.io/ws/v3/YOUR_KEY \
     blockchain-tracker
   ```

3. **Test the deployment**:
   ```bash
   curl http://localhost:8080/tracking/status
   ```

## Troubleshooting

### Common Issues

**Issue**: Service fails to start
```
Error: Web3j connection failed
```

**Solution**:
- Verify environment variables are set correctly
- Check blockchain provider API keys
- Ensure network connectivity to provider endpoints

**Issue**: No transactions received
```
WebSocket connection established but no data
```

**Solution**:
- Verify the wallet/contract address is correct
- Check if the address has recent transactions
- Confirm the network is supported and configured

**Issue**: High memory usage
```
Java heap space error
```

**Solution**:
- Reduce monitored addresses/contracts
- Increase JVM heap size: `-Xmx2g`
- Implement transaction cleanup policies

### Logs and Monitoring

**View application logs**:
```bash
# With Maven
./mvnw spring-boot:run

# With Docker
docker logs <container-id>
```

**Check health endpoints**:
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
```

## Advanced Configuration

### Custom Network Configuration

Add support for additional networks by updating `application.yaml`:

```yaml
networks:
  endpoints:
    ethereum: ${ETHEREUM_WS_URL:}
    polygon: ${POLYGON_WS_URL:}
    bsc: ${BSC_WS_URL:}  # Add Binance Smart Chain
```

### Performance Tuning

Adjust resilience settings in `application.yaml`:

```yaml
resilience4j:
  retry:
    instances:
      web3:
        max-attempts: 5
        wait-duration: 2s
  circuitbreaker:
    instances:
      web3:
        failure-rate-threshold: 60
        wait-duration-in-open-state: 10s
```

## Next Steps

1. **Integration Testing**: Run the full test suite
   ```bash
   ./mvnw test
   ```

2. **Load Testing**: Simulate high-volume transaction scenarios

3. **Production Deployment**: Configure production provider endpoints and monitoring

4. **Feature Extensions**: Add support for additional networks or monitoring types

## Support

For issues or questions:
- Check application logs for detailed error messages
- Verify blockchain provider status and API limits
- Review the API documentation in `/contracts/api.yaml`
- Test with known active addresses/contracts first