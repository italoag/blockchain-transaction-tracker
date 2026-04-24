# SRD (Software Requirements Document)
## Plataforma de Rastreamento Blockchain EVM

- **Versão:** 1.0
- **Data:** 2026-04-24
- **Status:** Draft para validação
- **Base:** `docs/plano-implementacao-rastreamento-evm.md`

---

## 1. Visão Geral

### 1.1 Propósito
Definir requisitos funcionais e não funcionais para um sistema capaz de rastrear transações, carteiras, tokens e contratos em redes EVM, incluindo correlação de fluxos entre cadeias por bridges e mecanismos equivalentes.

### 1.2 Escopo
A solução deve entregar:
- ingestão near real-time de dados on-chain;
- normalização e enriquecimento de transferências;
- monitoramento por watchlists;
- APIs de consulta e alertas;
- correlação cross-chain com score de confiança e evidências auditáveis.

### 1.3 Definições
- **EVM:** Ethereum Virtual Machine.
- **Bridge:** Protocolo de transferência de valor entre redes.
- **CrossChainLink:** Relação inferida entre evento origem e destino em redes distintas.
- **Watchlist:** Conjunto de carteiras/contratos monitorados por regras.

---

## 2. Stakeholders
- **Produto:** priorização de roadmap e valor de negócio.
- **Engenharia Backend/Data:** implementação e operação dos serviços.
- **SRE/DevOps:** confiabilidade, infraestrutura e observabilidade.
- **Segurança/Compliance:** controles, auditoria e governança.
- **Usuário final (analista):** consulta, investigação e alertas.

---

## 3. Objetivos e Métricas de Sucesso

### 3.1 Objetivos
1. Rastrear entradas/saídas de carteiras em múltiplas redes EVM.
2. Detectar e correlacionar transferências cross-chain com explicabilidade.
3. Disponibilizar APIs e alertas com baixa latência operacional.

### 3.2 KPIs
- Cobertura de indexação por rede (% blocos processados).
- Latência P95 de ingestão (confirmação -> disponibilidade).
- Taxa de sucesso de alertas entregues.
- Precisão de correlação de bridges (amostra validada).

---

## 4. Escopo Funcional

### 4.1 Em escopo (MVP)
- Redes EVM prioritárias configuráveis.
- Indexação de tx nativas + eventos ERC-20.
- Cadastro de watchlists.
- Consulta por carteira, token, rede e período.
- Alertas por regras simples.
- Detecção de bridge v1 com score e evidências.

### 4.2 Fora de escopo (MVP)
- Cobertura universal de todos os protocolos de bridge.
- Forense avançada multi-hop com IA.
- Regras de risco completas AML-grade.

---

## 5. Requisitos Funcionais (RF)

### RF-001 Ingestão de blocos
O sistema deve consumir blocos e transações de redes EVM suportadas em near real-time.

### RF-002 Ingestão de logs
O sistema deve coletar logs de eventos de contratos via RPC/WebSocket com fallback.

### RF-003 Persistência bruta
O sistema deve armazenar dados brutos de bloco/tx/log para auditoria e replay.

### RF-004 Decodificação de eventos
O sistema deve decodificar eventos com base em ABIs conhecidas e fallback de classificação por tópico.

### RF-005 Normalização de transferências
O sistema deve converter eventos em modelo canônico de transferência (nativa e ERC-20).

### RF-006 Deduplicação
O sistema deve evitar duplicidades com chave natural (`chain_id+tx_hash+log_index`).

### RF-007 Watchlist
O sistema deve permitir cadastro, atualização e remoção de carteiras monitoradas.

### RF-008 Regras de alerta
O sistema deve permitir regras por wallet/token/rede/faixa de valor.

### RF-009 Entrega de alertas
O sistema deve enviar alertas por webhook e registrar status de entrega, retry e DLQ.

### RF-010 Consulta por carteira
O sistema deve retornar histórico de entradas/saídas com paginação e filtros.

### RF-011 Consulta por token
O sistema deve permitir consultar fluxo de um token por rede e período.

### RF-012 Metadados de contratos/tokens
O sistema deve manter catálogo de contratos, bridges e metadados de tokens.

### RF-013 Detecção de bridge v1
O sistema deve identificar eventos candidatos de bridge com base em contratos e heurísticas conhecidas.

### RF-014 Correlação cross-chain
O sistema deve correlacionar origem/destino entre redes em janela temporal configurável.

### RF-015 Score de confiança
O sistema deve atribuir score para cada correlação e armazenar fatores contribuintes.

### RF-016 Evidências
O sistema deve persistir evidências de inferência para auditoria (eventos/regra/janela/tolerância).

### RF-017 Tratamento de reorg
O sistema deve detectar reorganização de bloco e executar rollback/replay idempotente.

### RF-018 APIs de status
O sistema deve expor estado de saúde e atraso de indexação por rede.

### RF-019 RBAC
O sistema deve aplicar controle de acesso por tenant/perfil para APIs administrativas.

### RF-020 Trilha de auditoria
O sistema deve registrar ações sensíveis (mudanças em regras/catálogo/credenciais).

---

## 6. Requisitos Não Funcionais (RNF)

### RNF-001 Disponibilidade
API pública com disponibilidade mensal mínima de 99,5% (MVP).

### RNF-002 Latência de ingestão
P95 de até 2 minutos após confirmação de bloco.

### RNF-003 Latência de alerta
P95 de até 3 minutos após disponibilidade da transferência.

### RNF-004 Escalabilidade
Arquitetura deve escalar horizontalmente componentes de ingestão e correlação.

### RNF-005 Segurança de dados
Dados em trânsito com TLS e dados sensíveis em repouso criptografados.

### RNF-006 Gestão de segredos
Segredos devem ser geridos via Vault/KMS, sem hardcode no código.

### RNF-007 Observabilidade
Métricas, logs estruturados e tracing distribuído obrigatórios.

### RNF-008 Resiliência
Suporte a fallback de provedores RPC, retries e circuit breakers.

### RNF-009 Confiabilidade de processamento
Jobs idempotentes e replay seguro sem efeitos colaterais indevidos.

### RNF-010 Compliance
Aderência a LGPD/GDPR para dados de usuários da plataforma.

---

## 7. Casos de Uso (alto nível)

### UC-01 Monitorar carteira
**Ator:** Analista
1. Cadastra carteira na watchlist.
2. Define regra de alerta.
3. Recebe alerta quando houver entrada compatível.

### UC-02 Investigar origem cross-chain
**Ator:** Analista
1. Seleciona transação recebida em carteira alvo.
2. Consulta correlação cross-chain.
3. Visualiza score, tx origem/destino e evidências.

### UC-03 Operar catálogo de bridges
**Ator:** Admin
1. Cadastra/atualiza contratos de bridge por rede.
2. Publica versão da regra.
3. Sistema passa a usar regra nova com versionamento.

---

## 8. Regras de Negócio
- **RN-01:** Transferências só são consideradas "confirmadas" após N blocos por rede.
- **RN-02:** Correlação cross-chain deve possuir score mínimo configurável para aparecer como "confirmada".
- **RN-03:** Casos abaixo do score mínimo permanecem como "suspeita" para revisão.
- **RN-04:** Catálogo de bridges deve ser versionado e auditável.
- **RN-05:** Alterações em regras impactam apenas processamentos futuros, salvo reprocessamento explícito.

---

## 9. Arquitetura Lógica

### 9.1 Serviços
- `ingestion-service`
- `decoder-service`
- `normalization-service`
- `bridge-correlation-service`
- `watchlist-alert-service`
- `query-api-service`
- `metadata-service`

### 9.2 Mensageria e armazenamento
- Tópicos/eventos internos por estágio de processamento.
- Banco relacional para consulta operacional.
- Cache para redução de latência em consultas recorrentes.

### 9.3 Estratégia de consistência
- Escrita transacional por estágio.
- Chave idempotente global por evento de transferência.
- Mecanismo de compensação em reorg.

---

## 10. Modelo de Dados (visão SRD)

### 10.1 Tabelas principais
- `chains`
- `blocks`
- `transactions`
- `event_logs`
- `token_transfers`
- `wallet_watchlists`
- `bridge_protocols`
- `cross_chain_links`
- `alert_rules`
- `alerts`
- `audit_logs`

### 10.2 Campos obrigatórios de auditoria
- `created_at`, `updated_at`, `created_by`, `version`, `source_system`.

---

## 11. APIs (requisitos de contrato)

### 11.1 Endpoints mínimos
- `POST /watchlists`
- `GET /wallets/{address}/transfers`
- `GET /wallets/{address}/inflows`
- `GET /tokens/{token}/flows`
- `GET /bridges/links/{txHash}`
- `POST /alerts/rules`
- `GET /health/indexing`

### 11.2 Requisitos de API
- Paginação cursor-based para consultas volumosas.
- Filtros por rede/período/token/valor.
- Versionamento de API (`/v1`).
- Autenticação e autorização por token.

---

## 12. Segurança
- Autenticação via OAuth2/JWT (ou equivalente corporativo).
- RBAC por tenant/workspace.
- HMAC em webhooks de saída.
- Rate limiting por cliente.
- Proteção contra replay em webhooks.

---

## 13. Observabilidade e Operação

### 13.1 Métricas obrigatórias
- atraso de indexação por rede;
- throughput por estágio;
- taxa de erro por endpoint RPC;
- filas (lag, retries, DLQ);
- latência P95/P99 por endpoint de API.

### 13.2 Alertas operacionais
- ingestão parada por rede;
- aumento anormal de falhas de correlação;
- indisponibilidade de provedor RPC;
- falha de entrega de webhook acima de limiar.

---

## 14. Estratégia de Testes e Validação
- Unitários para regras de domínio.
- Integração com ambiente de nós EVM.
- Contrato de APIs (OpenAPI tests).
- Carga (stress e soak) para ingestão e consulta.
- Simulação de reorg e falha de provider.
- Validação de precisão de correlação em dataset anotado.

---

## 15. Critérios de Aceite (MVP)
1. Indexação contínua em ao menos 2 redes EVM com latência dentro de SLO.
2. Consultas de carteira/token funcionais com paginação.
3. Alertas entregues com taxa de sucesso mínima definida.
4. Detecção de bridge v1 operando para protocolos prioritários.
5. Evidências de correlação disponíveis para auditoria.
6. Dashboards de observabilidade com métricas essenciais.

---

## 16. Premissas
- Disponibilidade de provedores RPC estáveis.
- Catálogo inicial de contratos de bridge será fornecido/curado.
- Time com capacidade para operação 24x7 em produção.

## 17. Restrições
- Precisão inicial de correlação depende de cobertura do catálogo.
- Protocolos proprietários sem documentação terão suporte gradual.
- Custos de indexação histórica total podem exigir faseamento.

## 18. Dependências
- Provedores RPC/WebSocket multi-chain.
- Infra de mensageria e banco.
- Ferramentas de observabilidade.
- Sistema de gestão de segredos.

---

## 19. Cronograma de Referência
- **Fase 0:** Fundação (CI/CD, infra, padrões).
- **Fase 1:** Ingestão + normalização + APIs básicas.
- **Fase 2:** Watchlist + alertas + dashboard MVP.
- **Fase 3:** Bridge correlation v1 + evidências.
- **Fase 4:** Hardening, compliance e escala.

---

## 20. Questões em Aberto
1. Redes prioritárias e ordem final de entrega.
2. Protocolos de bridge obrigatórios em V1.
3. SLO alvo definitivo por perfil de cliente.
4. Estratégia de nós próprios vs terceiros.
5. Requisitos regulatórios mandatórios para go-live.

---

## 21. Aprovação
Este documento deve ser revisado e aprovado por Produto, Engenharia, Segurança e Operações antes da execução de desenvolvimento em produção.
