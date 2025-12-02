# Blockchain Transaction Tracking Service

Este projeto demonstra um microserviço simples em Spring Boot WebFlux para acompanhamento de transações em diferentes redes blockchain.

O serviço utiliza a biblioteca **web3j 4.12** para se conectar as redes e monitorar novos blocos e transações.

## Desenvolvimento

```bash
./mvnw spring-boot:run
```

### Docker

```bash
docker-compose up --build
```

### Code Structure

```text
blockchain-connector/
│
├── src/
│   └── main/
        └── java/
            └── com/
                └── project/
                    └── blockchain/
                        └── connector/
                            │
                            ├── domain/                                 # CAMADA DE DOMÍNIO (núcleo da aplicação)
                            │   │
                            │   ├── model/                              # Entidades e Agregados
                            │   │   ├── transaction/
                            │   │   ├── signature/
                            │   │   ├── blockchain/
                            │   │   └── retry/
                            │   │
                            │   ├── event/                              # Domain Events
                            │   │
                            │   ├── exception/                          # Domain Exceptions
                            │   │
                            │   ├── port/                               # Portas (interfaces)
                            │   │   ├── inbound/                        # Casos de uso (entradas)
                            │   │   └── outbound/                       # Portas de saída
                            │   │
                            │   └── service/                            # Domain Services
                            │
                            ├── application/                            # CAMADA DE APLICAÇÃO
                            │   │
                            │   ├── usecase/                            # Implementação dos casos de uso
                            │   │
                            │   ├── service/                            # Application Services (orquestração)
                            │   │
                            │   ├── statemachine/                       # State Machine
                            │   │   ├── config/
                            │   │   ├── state/
                            │   │   ├── event/
                            │   │   ├── action/
                            │   │   └── guard/
                            │   │
                            │   ├── mapper/                             # Application DTOs e Mappers
                            │   └── dto/                                # DTOs da camada de aplicação
                            │
                            └── infrastructure/                         # CAMADA DE INFRAESTRUTURA
                                │
                                ├── adapter/                            # Adaptadores
                                │   │
                                │   ├── inbound/                        # Adaptadores de entrada
                                │   │   ├── messaging/
                                │   │   │   ├── kafka/
                                │   │   │   │   ├── consumer/
                                │   │   │   │   ├── config/
                                │   │   │   │   └── dto/
                                │   │   │   └── mapper/
                                │   │   └── rest/
                                │   │       ├── controller/
                                │   │       ├── dto/
                                │   │       └── mapper/
                                │   │
                                │   └── outbound/                       # Adaptadores de saída
                                │       ├── persistence/
                                │       │   ├── mongodb/
                                │       │   │   ├── repository/
                                │       │   │   ├── entity/
                                │       │   │   ├── mapper/
                                │       │   │   └── config/
                                │       ├── blockchain/
                                │       │   ├── web3j/
                                │       │   │   ├── config/
                                │       │   │   ├── mapper/
                                │       │   │   └── util/
                                │       │   │
                                │       │   └── dto/
                                │       ├── signer/
                                │       │   ├── kafka/
                                │       │   ├── rest/
                                │       │   ├── strategy/
                                │       │   └── dto/
                                │       ├── messaging/
                                │       │   ├── kafka/
                                │       │   │   ├── producer/
                                │       │   │   └── mapper/
                                │       └── cache/
                                │           └── config/
                                ├── config/                      # Configurações gerais
                                │   └── properties/
                                ├── scheduler/                   # Tarefas agendadas
                                ├── resilience/                  # Resiliência
                                ├── observability/               # Observabilidade
                                │   ├── metrics/
                                │   ├── tracing/
                                │   └── logging/
                                └── exception/                   # Exception Handlers
```

### Propose 2

```text
blockchain-connector/
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/example/blockchainconnector/
    │   │       ├── boot/
    │   │       │   ├── BlockchainConnectorApplication.java
    │   │       │   └── StartupRunner.java
    │   │       │
    │   │       ├── domain/
    │   │       │   ├── model/
    │   │       │   │   ├── transaction/
    │   │       │   │   │   ├── BlockchainTransaction.java
    │   │       │   │   │   ├── TransactionStatus.java
    │   │       │   │   │   ├── GasEstimation.java
    │   │       │   │   │   └── Nonce.java
    │   │       │   │   ├── wallet/
    │   │       │   │   │   └── Wallet.java
    │   │       │   │   └── event/
    │   │       │   │       └── ChainEvent.java
    │   │       │   │
    │   │       │   ├── exception/
    │   │       │   │   ├── TransactionNotFoundException.java
    │   │       │   │   └── SignatureException.java
    │   │       │   │
    │   │       │   ├── port/
    │   │       │   │   ├── inbound/
    │   │       │   │   │   ├── KafkaEventListenerPort.java
    │   │       │   │   │   └── ApiInboundPort.java
    │   │       │   │   ├── outbound/
    │   │       │   │   │   ├── SignerRestClientPort.java
    │   │       │   │   │   ├── SignerEventProducerPort.java
    │   │       │   │   │   ├── BlockchainClientPort.java
    │   │       │   │   │   ├── TransactionRepositoryPort.java
    │   │       │   │   │   ├── GasEstimatorPort.java
    │   │       │   │   │   ├── NonceManagerPort.java
    │   │       │   │   │   └── ConfirmationListenerPort.java
    │   │       │   │   └── state/
    │   │       │   │       ├── StateMachinePort.java
    │   │       │   │       └── StateTransitionCallbackPort.java
    │   │       │   │
    │   │       │   └── service/
    │   │       │       ├── TransactionService.java
    │   │       │       ├── GasService.java
    │   │       │       ├── NonceService.java
    │   │       │       └── ConfirmationService.java
    │   │       │
    │   │       ├── application/
    │   │       │   ├── dto/
    │   │       │   │   ├── request/
    │   │       │   │   │   ├── CreateTransactionRequest.java
    │   │       │   │   │   └── SignerCallbackRequest.java
    │   │       │   │   ├── response/
    │   │       │   │   │   └── TransactionStatusResponse.java
    │   │       │   │   └── event/
    │   │       │   │       ├── OutboundTransactionEvent.java
    │   │       │   │       └── ConfirmationEventPayload.java
    │   │       │   │
    │   │       │   ├── mapper/
    │   │       │   │   ├── TransactionMapper.java
    │   │       │   │   ├── SignerMapper.java
    │   │       │   │   └── EventMapper.java
    │   │       │   │
    │   │       │   ├── usecase/
    │   │       │   │   ├── CreateTransactionUseCase.java
    │   │       │   │   ├── HandleSignatureCallbackUseCase.java
    │   │       │   │   ├── BroadcastTransactionUseCase.java
    │   │       │   │   ├── WaitForConfirmationUseCase.java
    │   │       │   │   └── CompleteFlowUseCase.java
    │   │       │   │
    │   │       │   ├── statemachine/
    │   │       │   │   ├── config/
    │   │       │   │   │   └── BlockchainStateMachineConfig.java
    │   │       │   │   ├── state/
    │   │       │   │   │   ├── BlockchainStates.java
    │   │       │   │   │   └── BlockchainEvents.java
    │   │       │   │   └── handler/
    │   │       │   │       ├── StartupStateHandler.java
    │   │       │   │       └── StateTransitionListener.java
    │   │       │   │
    │   │       │   └── orchestrator/
    │   │       │       │   └── TransactionOrchestrator.java
    │   │       │
    │   │       ├── infrastructure/
    │   │       │   ├── config/
    │   │       │   │   ├── KafkaConfig.java
    │   │       │   │   ├── MongoConfig.java
    │   │       │   │   ├── Web3jConfig.java
    │   │       │   │   ├── WebClientConfig.java
    │   │       │   │   └── StateMachineConfig.java
    │   │       │   │
    │   │       │   ├── adapter/
    │   │       │   │   ├── inbound/
    │   │       │   │   │   ├── api/
    │   │       │   │   │   │   └── TransactionController.java
    │   │       │   │   │   └── kafka/
    │   │       │   │   │       └── TransactionEventConsumer.java
    │   │       │   │   ├── outbound/
    │   │       │   │   │   ├── kafka/
    │   │       │   │   │   │   └── ConfirmationEventProducer.java
    │   │       │   │   │   ├── rest/
    │   │       │   │   │   │   ├── SignerRestClient.java
    │   │       │   │   │   │   └── ExternalApiClient.java
    │   │       │   │   │   ├── blockchain/
    │   │       │   │   │   │   ├── Web3jBlockchainClient.java
    │   │       │   │   │   │   ├── GasEstimatorWeb3j.java
    │   │       │   │   │   │   └── NonceManagerWeb3j.java
    │   │       │   │   │   └── repository/
    │   │       │   │   │       └── MongoTransactionRepository.java
    │   │       │   │
    │   │       │   └── security/
    │   │       │       └── HttpSecurityConfig.java
    │   │       │
    │   │       └── shared/
    │   │           ├── annotation/
    │   │           │   └── DomainService.java
    │   │           ├── util/
    │   │           │   ├── JsonUtils.java
    │   │           │   └── RetryUtils.java
    │   │           └── events/
    │   │               └── KafkaHeadersConstants.java
    │   │
    │   └── resources/
    │       ├── application.yml
    │       ├── state-machine/
    │       │   └── blockchain-state-machine.sm
    │       └── mappings/
    │           └── transaction-mapper.xml
    │
    └── test/
        └── java/
            └── com/example/blockchainconnector/
                ├── domain/
                ├── application/
                ├── infrastructure/
                └── integration/
```

### Propose 3

```text
blockchain-connector/
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/example/blockchainconnector/
    │   │       ├── boot/
    │   │       │   ├── BlockchainConnectorApplication.java
    │   │       │   └── StartupRunner.java
    │   │       │
    │   │       ├── domain/
    │   │       │   ├── model/
    │   │       │   │   ├── transaction/
    │   │       │   │   │   ├── BlockchainTransaction.java
    │   │       │   │   │   ├── TransactionStatus.java
    │   │       │   │   │   ├── GasEstimation.java
    │   │       │   │   │   └── Nonce.java
    │   │       │   │   ├── wallet/
    │   │       │   │   │   └── Wallet.java
    │   │       │   │   └── event/
    │   │       │   │       └── ChainEvent.java
    │   │       │   │
    │   │       │   ├── exception/
    │   │       │   │   ├── TransactionNotFoundException.java
    │   │       │   │   └── SignatureException.java
    │   │       │   │
    │   │       │   ├── port/
    │   │       │   │   ├── inbound/
    │   │       │   │   │   ├── KafkaEventListenerPort.java
    │   │       │   │   │   └── ApiInboundPort.java
    │   │       │   │   ├── outbound/
    │   │       │   │   │   ├── SignerRestClientPort.java
    │   │       │   │   │   ├── SignerEventProducerPort.java
    │   │       │   │   │   ├── BlockchainClientPort.java
    │   │       │   │   │   ├── TransactionRepositoryPort.java
    │   │       │   │   │   ├── GasEstimatorPort.java
    │   │       │   │   │   ├── NonceManagerPort.java
    │   │       │   │   │   └── ConfirmationListenerPort.java
    │   │       │   │   └── state/
    │   │       │   │       ├── StateMachinePort.java
    │   │       │   │       └── StateTransitionCallbackPort.java
    │   │       │   │
    │   │       │   └── service/
    │   │       │       ├── TransactionService.java
    │   │       │       ├── GasService.java
    │   │       │       ├── NonceService.java
    │   │       │       └── ConfirmationService.java
    │   │       │
    │   │       ├── application/
    │   │       │   ├── dto/
    │   │       │   │   ├── request/
    │   │       │   │   │   ├── CreateTransactionRequest.java
    │   │       │   │   │   └── SignerCallbackRequest.java
    │   │       │   │   ├── response/
    │   │       │   │   │   └── TransactionStatusResponse.java
    │   │       │   │   └── event/
    │   │       │   │       ├── OutboundTransactionEvent.java
    │   │       │   │       └── ConfirmationEventPayload.java
    │   │       │   │
    │   │       │   ├── mapper/
    │   │       │   │   ├── TransactionMapper.java
    │   │       │   │   ├── SignerMapper.java
    │   │       │   │   └── EventMapper.java
    │   │       │   │
    │   │       │   ├── usecase/
    │   │       │   │   ├── CreateTransactionUseCase.java
    │   │       │   │   ├── HandleSignatureCallbackUseCase.java
    │   │       │   │   ├── BroadcastTransactionUseCase.java
    │   │       │   │   ├── WaitForConfirmationUseCase.java
    │   │       │   │   └── CompleteFlowUseCase.java
    │   │       │   │
    │   │       │   ├── statemachine/
    │   │       │   │   ├── config/
    │   │       │   │   │   └── BlockchainStateMachineConfig.java
    │   │       │   │   ├── state/
    │   │       │   │   │   ├── BlockchainStates.java
    │   │       │   │   │   └── BlockchainEvents.java
    │   │       │   │   └── handler/
    │   │       │   │       ├── StartupStateHandler.java
    │   │       │   │       └── StateTransitionListener.java
    │   │       │   │
    │   │       │   └── orchestrator/
    │   │       │       │   └── TransactionOrchestrator.java
    │   │       │
    │   │       ├── infrastructure/
    │   │       │   ├── config/
    │   │       │   │   ├── KafkaConfig.java
    │   │       │   │   ├── MongoConfig.java
    │   │       │   │   ├── Web3jConfig.java
    │   │       │   │   ├── WebClientConfig.java
    │   │       │   │   └── StateMachineConfig.java
    │   │       │   │
    │   │       │   ├── adapter/
    │   │       │   │   ├── inbound/
    │   │       │   │   │   ├── api/
    │   │       │   │   │   │   └── TransactionController.java
    │   │       │   │   │   └── kafka/
    │   │       │   │   │       └── TransactionEventConsumer.java
    │   │       │   │   ├── outbound/
    │   │       │   │   │   ├── kafka/
    │   │       │   │   │   │   └── ConfirmationEventProducer.java
    │   │       │   │   │   ├── rest/
    │   │       │   │   │   │   ├── SignerRestClient.java
    │   │       │   │   │   │   └── ExternalApiClient.java
    │   │       │   │   │   ├── blockchain/
    │   │       │   │   │   │   ├── Web3jBlockchainClient.java
    │   │       │   │   │   │   ├── GasEstimatorWeb3j.java
    │   │       │   │   │   │   └── NonceManagerWeb3j.java
    │   │       │   │   │   └── repository/
    │   │       │   │   │       └── MongoTransactionRepository.java
    │   │       │   │
    │   │       │   └── security/
    │   │       │       └── HttpSecurityConfig.java
    │   │       │
    │   │       └── shared/
    │   │           ├── annotation/
    │   │           │   └── DomainService.java
    │   │           ├── util/
    │   │           │   ├── JsonUtils.java
    │   │           │   └── RetryUtils.java
    │   │           └── events/
    │   │               └── KafkaHeadersConstants.java
    │   │
    │   └── resources/
    │       ├── application.yml
    │       ├── state-machine/
    │       │   └── blockchain-state-machine.sm
    │       └── mappings/
    │           └── transaction-mapper.xml
    │
    └── test/
        └── java/
            └── com/example/blockchainconnector/
                ├── domain/
                ├── application/
                ├── infrastructure/
                └── integration/
```

### Propose 4

```
src/
└── main/
    ├── java/
    │   └── com/
    │       └── tech/
    │           └── blockchainconnector/
    │               ├── BlockchainConnectorApplication.java
    │               │
    │               ├── domain/                         # O NÚCLEO (Sem dependências de Frameworks pesados)
    │               │   ├── model/                      # Entidades e Value Objects
    │               │   │   ├── Transaction.java        # Aggregate Root (State, hash, payload)
    │               │   │   ├── TransactionId.java
    │               │   │   ├── NetworkConfig.java      # Configuração da Chain (RPC, ChainId)
    │               │   │   └── GasInfo.java
    │               │   ├── event/                      # Eventos de Domínio (Internos)
    │               │   │   ├── TransactionSignedEvent.java
    │               │   │   └── TransactionConfirmedEvent.java
    │               │   ├── exception/                  # Exceções de Domínio
    │               │   │   └── InsufficientFundsException.java
    │               │   └── port/                       # Portas (Interfaces)
    │               │       ├── inbound/                # Portas de Entrada (Use Cases interfaces)
    │               │       │   └── ProcessTransactionUseCase.java
    │               │       └── outbound/               # Portas de Saída (Interfaces para Infra)
    │               │           ├── TransactionRepository.java
    │               │           ├── BlockchainClientPort.java  # Web3j Abstraction
    │               │           ├── SignerClientPort.java      # Hybrid (Kafka/Rest)
    │               │           └── EventPublisherPort.java    # Publicação no Kafka (Notification)
    │               │
    │               ├── application/                    # CAMADA DE APLICAÇÃO
    │               │   ├── dto/                        # DTOs de Entrada/Saída
    │               │   │   ├── TransactionRequestDTO.java
    │               │   │   └── TransactionResponseDTO.java
    │               │   ├── mapper/                     # MapStruct Interfaces
    │               │   │   └── TransactionMapper.java
    │               │   └── service/                    # Implementação dos Use Cases
    │               │       ├── TransactionOrchestrator.java
    │               │       └── RecoveryService.java    # Lógica de reprocessamento em restart
    │               │
    │               └── infrastructure/                 # ADAPTADORES (Tecnologia real)
    │                   ├── config/                     # Configurações do Spring
    │                   │   ├── KafkaConfig.java
    │                   │   ├── Web3jConfig.java
    │                   │   ├── OpenTelemetryConfig.java
    │                   │   ├── MongoConfig.java
    │                   │   └── StateMachineConfig.java # Definição dos Estados e Transições
    │                   │
    │                   ├── adapter/
    │                   │   ├── inbound/                # Quem chama a aplicação
    │                   │   │   ├── kafka/              # Listeners do Kafka
    │                   │   │   │   └── TransactionRequestConsumer.java
    │                   │   │   └── scheduler/          # Jobs recorrentes
    │                   │   │       └── StuckTransactionRecoveryJob.java
    │                   │   │
    │                   │   └── outbound/               # Quem a aplicação chama
    │                   │       ├── persistence/        # MongoDB
    │                   │       │   ├── MongoTransactionRepository.java # Impl do Port
    │                   │       │   └── entity/         # Entidades JPA/Mongo (separadas do Domain)
    │                   │       │       └── TransactionDocument.java
    │                   │       ├── blockchain/         # Web3j
    │                   │       │   ├── Web3jClientAdapter.java
    │                   │       │   └── NonceManager.java
    │                   │       ├── signer/             # Comunicação com Signer
    │                   │       │   ├── HybridSignerAdapter.java # Lógica Kafka c/ fallback REST
    │                   │       │   └── rest/           # WebClient impl
    │                   │       └── messaging/          # Kafka Producer
    │                   │           └── KafkaEventPublisher.java
    │                   │
    │                   └── statemachine/               # Lógica específica do Spring State Machine
    │                       ├── TransactionStateMachineInterceptor.java
    │                       ├── actions/                # Ações disparadas nas trocas de estado
    │                       │   ├── EstimateGasAction.java
    │                       │   ├── RequestSignatureAction.java
    │                       │   └── BroadcastToChainAction.java
    │                       └── guards/                 # Validações antes das transições
    │                           └── BalanceGuard.java
    │
    └── resources/
        ├── application.yml
        ├── application-prod.yml
        └── statemachine-diagram.puml # Documentação visual da SM

```
