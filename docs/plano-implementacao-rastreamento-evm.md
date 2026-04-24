# Plano Detalhado de Implementação
## Serviço de Rastreamento de Transações, Carteiras, Tokens e Contratos em Redes EVM

## 1. Objetivo
Construir uma plataforma de monitoramento e rastreamento blockchain para redes EVM, com foco em:

- rastreamento de transações e eventos de contratos;
- monitoramento de carteiras e fluxos de entrada/saída;
- correlação de transferências entre redes (bridges e ferramentas equivalentes);
- explicabilidade e auditabilidade das correlações de fluxo cross-chain;
- alta confiabilidade, escalabilidade horizontal e observabilidade operacional.

## 2. Escopo do Produto

### 2.1 Escopo funcional (MVP)
- Monitorar blocos e transações em múltiplas redes EVM.
- Indexar transferências nativas e eventos ERC-20.
- Permitir cadastro de carteiras monitoradas (watchlists).
- Expor APIs para consulta por carteira/token/rede/período.
- Gerar alertas de recebimento baseado em regras.
- Detectar sinais iniciais de transferência via bridge.

### 2.2 Escopo evolutivo (fases posteriores)
- Suporte a ERC-721 e ERC-1155.
- Correlação avançada lock/mint, burn/release e message passing.
- Grafo de fluxo multi-hop com score de confiança.
- Classificação de entidades (DEX, bridge, CEX hot wallet, mixer).
- Inteligência de risco e anomalias.

## 3. Diretrizes de Arquitetura

### 3.1 Princípios
- Arquitetura orientada a eventos.
- Processamento idempotente e reprocessável.
- Separação entre ingestão, normalização, enriquecimento e consulta.
- "Exactly-once effect" na camada de domínio via deduplicação.
- Resiliência a reorg e falhas de provedores RPC.

### 3.2 Componentes principais
1. **Ingestion Service**: captura blocos, transações e logs.
2. **Decoder Service**: decodifica eventos via ABI e normaliza payloads.
3. **Transfer Normalizer**: transforma eventos em transferências canônicas.
4. **Bridge Correlation Engine**: correlaciona origem/destino cross-chain.
5. **Watchlist & Alerting Service**: regras e notificações.
6. **API Gateway + Query Service**: consumo por UI e integrações.
7. **Metadata/Labeling Service**: labels de contratos e entidades.
8. **Observability Stack**: métricas, logs, tracing e alertas operacionais.

### 3.3 Estratégia de dados
- **PostgreSQL** para dados operacionais e consultas de API.
- **Redis** para cache de leituras quentes.
- **Kafka (ou equivalente)** para eventos internos.
- Particionamento por rede e faixa temporal em tabelas de alto volume.
- Índices compostos orientados a filtros usuais (wallet, token, timestamp, chain).

## 4. Modelo de Domínio (alto nível)

Entidades essenciais:
- `Chain`
- `Block`
- `Transaction`
- `EventLog`
- `Token`
- `TokenTransfer`
- `Wallet`
- `Contract`
- `BridgeProtocol`
- `BridgeEvent`
- `CrossChainLink`
- `AlertRule`
- `Alert`
- `EntityLabel`

Campos críticos para rastreabilidade:
- `chain_id`, `block_number`, `block_hash`, `tx_hash`, `log_index`, `trace_id`;
- `from_address`, `to_address`, `token_address`, `amount_raw`, `amount_normalized`;
- `detected_by_rule`, `confidence_score`, `evidence_payload`.

## 5. Fluxos de Processamento

### 5.1 Fluxo de ingestão e normalização
1. Capturar novo bloco e logs.
2. Persistir eventos brutos (`raw_events`).
3. Decodificar e classificar eventos.
4. Gerar transferências normalizadas.
5. Executar deduplicação por chave natural (`chain_id+tx_hash+log_index`).
6. Publicar para consumidores de enriquecimento e alerta.

### 5.2 Fluxo de correlação de bridges
1. Identificar eventos candidatos em contratos mapeados.
2. Aplicar heurísticas por protocolo/roteador.
3. Buscar eventos correlatos em rede de destino em janela temporal.
4. Validar consistência de valor (com tolerância de taxa/slippage).
5. Gerar `CrossChainLink` com score e evidências.
6. Permitir revisão manual de casos ambíguos.

### 5.3 Fluxo de alertas
1. Avaliar regras por carteira/token/rede.
2. Disparar webhook/e-mail/fila interna.
3. Armazenar estado de entrega, tentativas e DLQ.

## 6. Estratégia de Detecção de Bridges

### 6.1 Estratégia por tipo
- **Lock/Mint**: lock em origem + mint de wrapped em destino.
- **Burn/Release**: burn de wrapped + release de ativo nativo.
- **Message-passing bridge**: evento de envio + execução remota.
- **Liquidity bridge**: transferência entre pools com roteadores.

### 6.2 Score de confiança
Compor score com pesos para:
- proximidade temporal;
- correspondência de montante;
- pares de contratos conhecidos;
- consistência de token canônico/wrapped;
- assinatura de rota conhecida.

### 6.3 Explicabilidade
Cada correlação deve armazenar:
- regras acionadas;
- eventos usados na inferência;
- links de tx origem/destino;
- limites de tolerância aplicados.

## 7. Segurança, Compliance e Governança
- Gestão de segredos com KMS/Vault.
- RBAC com escopo por tenant/workspace.
- Assinatura HMAC em webhooks.
- Trilha de auditoria de ações sensíveis.
- Políticas de retenção e minimização de dados (LGPD/GDPR).
- Criptografia em trânsito (TLS) e em repouso.
- SAST/DAST e varredura de dependências no CI.

## 8. Observabilidade e SRE

### 8.1 Métricas de negócio
- volume de transações indexadas por rede;
- atraso médio de indexação;
- taxa de correlação de bridge (e precisão estimada);
- alertas disparados por período.

### 8.2 Métricas operacionais
- taxa de erro RPC por provedor;
- latência de processamento por estágio;
- consumo de fila e backlog;
- taxas de retry e DLQ.

### 8.3 SLOs sugeridos (MVP)
- disponibilidade API: 99,5%.
- latência de ingestão (P95): até 2 min após confirmação.
- tempo de alerta (P95): até 3 min após ingestão.

## 9. Qualidade e Testes
- Testes unitários por domínio e serviços.
- Testes de contrato de APIs.
- Testes de integração com nós EVM (ambiente controlado).
- Testes de carga para endpoints e ingestão.
- Testes de caos (falha de RPC, timeout, reorg simulado).
- Testes de regressão para regras de bridge.

## 10. Roadmap de Implementação

### Sprint 0 (Fundação)
- Setup de arquitetura, CI/CD, padrões e observabilidade base.
- Infra de desenvolvimento e staging.

### Sprint 1-2 (Core de indexação)
- Ingestão e persistência de blocos/logs.
- Parser ERC-20 e normalização de transferências.
- Reorg handling inicial.

### Sprint 3-4 (Produto MVP)
- APIs de consulta.
- Watchlist e alertas.
- Dashboard inicial.

### Sprint 5-6 (Cross-chain v1)
- Catálogo de bridges prioritárias.
- Motor de correlação com score e evidências.

### Sprint 7+ (Hardening)
- otimização de performance,
- segurança avançada,
- governança e compliance,
- expansão de protocolos/redes.

## 11. Riscos e Mitigações
- **Instabilidade RPC**: multi-provider + fallback + circuit breaker.
- **Reorg frequente**: janelas de confirmação e rollback idempotente.
- **Falso positivo em bridge**: score + revisão manual.
- **Escala de dados**: particionamento e arquivamento.
- **Mudanças em protocolos**: catálogo versionado e monitoramento contínuo.

## 12. Perguntas de Descoberta (Backlog de decisões)

### Produto e negócio
1. Quem é o usuário principal do sistema no lançamento?
2. Qual é o caso de uso #1 que não pode falhar no MVP?
3. Quais SLAs de atualização/alerta são aceitáveis?
4. Multi-tenant é obrigatório no dia 1?

### Escopo técnico
5. Quais redes EVM entram na versão inicial (ordem de prioridade)?
6. Qual volume estimado de carteiras monitoradas e tx/dia?
7. Qual profundidade histórica é necessária no go-live?
8. MVP cobre apenas ERC-20 ou também NFTs?

### Cross-chain
9. Quais bridges/protocolos são obrigatórios na v1?
10. Detectar somente bridge ou também rotas via DEX/CEX?
11. Preferência: maior precisão ou maior cobertura?
12. Explicabilidade auditável é requisito obrigatório?

### Integrações e operação
13. Nós próprios, providers terceiros ou estratégia híbrida?
14. Quais integrações externas são obrigatórias (SIEM, BI, webhook, Slack)?
15. Existe base de labels inicial para seed?
16. Qual cloud/região e orçamento operacional mensal?

### Segurança e compliance
17. Quais certificações e controles são exigidos (SOC2/ISO/LGPD)?
18. Qual política de retenção de logs e auditoria?
19. Qual modelo de autorização por perfil/equipe?
20. Há requisitos legais específicos por jurisdição?
