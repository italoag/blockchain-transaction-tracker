# Technology Stack

## Core Framework
- **Spring Boot 3.5.3** with WebFlux for reactive programming
- **Java 21** as the runtime environment
- **Maven** as the build system with wrapper scripts

## Key Dependencies
- **Web3j 4.12.0** - Blockchain connectivity and interaction
- **Project Reactor** - Reactive streams implementation
- **Resilience4j 2.2.0** - Circuit breakers, retries, and fault tolerance
- **Google Guava 33.1.0** - Bloom filter implementation
- **Spring Boot Actuator** - Health checks and monitoring
- **Spring Boot DevTools** - Development hot reload

## Build & Development Commands

### Local Development
```bash
# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Package the application
./mvnw package

# Clean and rebuild
./mvnw clean install
```

### Docker
```bash
# Build and run with Docker Compose
docker-compose up --build

# Build Docker image
docker build -t wallet .
```

## Configuration
- **YAML-based configuration** in `application.yaml`
- **Environment variables** for network endpoints (ETHEREUM_WS_URL, POLYGON_WS_URL)
- **Resilience4j patterns** configured for web3 connections
- **GraalVM native compilation** support included