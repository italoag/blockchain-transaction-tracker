# Implementation Plan: Blockchain Monitoring Service

**Branch**: `002-com-base-nas` | **Date**: September 12, 2025 | **Spec**: /Users/italo/Projects/bloco/blockchain-transaction-tracker/specs/002-com-base-nas/spec.md
**Input**: Feature specification from `/specs/002-com-base-nas/spec.md`

## Execution Flow (/plan command scope)
```
1. Load feature spec from Input path
   → If not found: ERROR "No feature spec at {path}"
2. Fill Technical Context (scan for NEEDS CLARIFICATION)
   → Detect Project Type from context (web=frontend+backend, mobile=app+api)
   → Set Structure Decision based on project type
3. Evaluate Constitution Check section below
   → If violations exist: Document in Complexity Tracking
   → If no justification possible: ERROR "Simplify approach first"
   → Update Progress Tracking: Initial Constitution Check
4. Execute Phase 0 → research.md
   → If NEEDS CLARIFICATION remain: ERROR "Resolve unknowns"
5. Execute Phase 1 → contracts, data-model.md, quickstart.md, agent-specific template file (e.g., `CLAUDE.md` for Claude Code, `.github/copilot-instructions.md` for GitHub Copilot, or `GEMINI.md` for Gemini CLI).
6. Re-evaluate Constitution Check section
   → If new violations: Refactor design, return to Phase 1
   → Update Progress Tracking: Post-Design Constitution Check
7. Plan Phase 2 → Describe task generation approach (DO NOT create tasks.md)
8. STOP - Ready for /tasks command
```

**IMPORTANT**: The /plan command STOPS at step 7. Phases 2-4 are executed by other commands:
- Phase 2: /tasks command creates tasks.md
- Phase 3-4: Implementation execution (manual or via tools)

## Summary
Real-time blockchain monitoring service for Ethereum-compatible networks supporting wallet tracking, contract monitoring, transaction streaming, and event capture via WebSocket and HTTPS JSON-RPC protocols. Implementation will use reactive programming patterns with Web3j library for blockchain connectivity.

## Technical Context
**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot WebFlux 3.5.5, Web3j 4.13.0, Resilience4j, Project Reactor
**Storage**: In-memory (with repository pattern for future persistence)
**Testing**: JUnit 5, Reactor Test, StepVerifier for reactive streams
**Target Platform**: JVM/Linux server environment
**Project Type**: Single project (backend service)
**Performance Goals**: Real-time transaction processing, sub-second response times for API calls
**Constraints**: Support multiple blockchain networks simultaneously, handle high-volume transaction periods
**Scale/Scope**: Monitor multiple networks concurrently, handle thousands of transactions per minute

## Constitution Check
*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Simplicity**:
- Projects: 1 (single Spring Boot application)
- Using framework directly? (Spring Boot WebFlux used directly)
- Single data model? (Java records for immutable data structures)
- Avoiding patterns? (Repository pattern justified for reactive data access)

**Architecture**:
- EVERY feature as library? (Service classes organized by domain)
- Libraries listed: blockchain (Web3j integration), wallet (business logic)
- CLI per library: N/A (web service, not CLI application)
- Library docs: README.md and inline documentation

**Testing (NON-NEGOTIABLE)**:
- RED-GREEN-Refactor cycle enforced? (Unit tests written first, integration tests for Web3j providers)
- Git commits show tests before implementation? (Test files exist for current implementation)
- Order: Contract→Integration→E2E→Unit strictly followed? (Integration tests for providers, unit tests for services)
- Real dependencies used? (Actual Web3j connections to test networks)
- Integration tests for: new libraries, contract changes, shared schemas? (Web3j provider integration tests)
- FORBIDDEN: Implementation before test, skipping RED phase (Tests exist and are maintained)

**Observability**:
- Structured logging included? (Spring Boot Actuator for monitoring)
- Frontend logs → backend? (N/A - backend service only)
- Error context sufficient? (Resilience4j circuit breaker with error handling)

**Versioning**:
- Version number assigned? (0.1.0-SNAPSHOT in pom.xml)
- BUILD increments on every change? (Maven versioning)
- Breaking changes handled? (Semantic versioning with Spring Boot)

## Project Structure

### Documentation (this feature)
```
specs/002-com-base-nas/
├── plan.md              # This file (/plan command output)
├── research.md          # Phase 0 output (/plan command)
├── data-model.md        # Phase 1 output (/plan command)
├── quickstart.md        # Phase 1 output (/plan command)
├── contracts/           # Phase 1 output (/plan command)
└── tasks.md             # Phase 2 output (/tasks command - NOT created by /plan)
```

### Source Code (repository root)
```
# Option 1: Single project (DEFAULT)
src/
├── main/java/dev/bloco/
│   ├── blockchain/      # Web3j integration services
│   └── wallet/          # Business logic, handlers, models
└── test/java/dev/bloco/
    ├── blockchain/      # Integration tests for Web3j
    └── wallet/          # Unit tests for services
```

**Structure Decision**: Option 1 (Single project) - Backend service with clear package separation

## Phase 0: Outline & Research
1. **Extract unknowns from Technical Context**:
   - Web3j best practices for reactive programming
   - Resilience patterns for blockchain provider failures
   - Bloom filter optimization for transaction deduplication
   - WebSocket connection management for blockchain networks

2. **Generate and dispatch research agents**:
   ```
   For each unknown in Technical Context:
     Task: "Research reactive Web3j integration patterns"
     Task: "Research blockchain provider failover strategies"
     Task: "Research Bloom filter implementations for high-throughput deduplication"
     Task: "Research WebSocket connection management for blockchain networks"
   ```

3. **Consolidate findings** in `research.md` using format:
   - Decision: [what was chosen]
   - Rationale: [why chosen]
   - Alternatives considered: [what else evaluated]

**Output**: research.md with all technical decisions documented

## Phase 1: Design & Contracts
*Prerequisites: research.md complete*

1. **Extract entities from feature spec** → `data-model.md`:
   - Blockchain Network: connection endpoints, protocol support
   - Wallet Address: address string, network association
   - Smart Contract: contract address, ABI, events to monitor
   - Transaction: hash, from/to addresses, value, timestamp, network
   - Contract Event: contract address, event signature, parameters

2. **Generate API contracts** from functional requirements:
   - POST /tracking/transactions → start monitoring request
   - WebSocket /ws/tracking → real-time transaction stream
   - GET /tracking/status → monitoring status
   - Use OpenAPI 3.0 specification format

3. **Generate contract tests** from contracts:
   - One test file per endpoint
   - Assert request/response schemas
   - Tests must fail (no implementation yet)

4. **Extract test scenarios** from user stories:
   - Wallet monitoring scenario → track specific address
   - Contract monitoring scenario → capture events
   - Multi-network scenario → monitor multiple networks
   - Connection failure scenario → handle provider outages

5. **Update agent file incrementally**:
   - Update .github/copilot-instructions.md with new patterns
   - Preserve existing content, add blockchain-specific guidance
   - Keep under 150 lines for efficiency

**Output**: data-model.md, /contracts/*, failing tests, quickstart.md, updated copilot-instructions.md

## Phase 2: Task Planning Approach
*This section describes what the /tasks command will do - DO NOT execute during /plan*

**Task Generation Strategy**:
- Load `/templates/tasks-template.md` as base
- Generate tasks from Phase 1 design docs (contracts, data model, quickstart)
- Each API contract → contract test task [P]
- Each entity → model creation task [P]
- Each user scenario → integration test task
- Implementation tasks to make tests pass

**Ordering Strategy**:
- TDD order: Tests before implementation
- Dependency order: Models before services before handlers
- Mark [P] for parallel execution (independent files)

**Estimated Output**: 25-30 numbered, ordered tasks in tasks.md

**IMPORTANT**: This phase is executed by the /tasks command, NOT by /plan

## Phase 3+: Future Implementation
*These phases are beyond the scope of the /plan command*

**Phase 3**: Task execution (/tasks command creates tasks.md)
**Phase 4**: Implementation (execute tasks.md following constitutional principles)
**Phase 5**: Validation (run tests, execute quickstart.md, performance validation)

## Complexity Tracking
*Fill ONLY if Constitution Check has violations that must be justified*

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Repository pattern | Reactive data access abstraction | Direct service usage insufficient for testability |
| Custom Bloom filter | High-throughput deduplication needs | Guava BloomFilter lacks reactive integration |

## Progress Tracking
*This checklist is updated during execution flow*

**Phase Status**:
- [x] Phase 0: Research complete (/plan command)
- [x] Phase 1: Design complete (/plan command)
- [ ] Phase 2: Task planning complete (/plan command - describe approach only)
- [ ] Phase 3: Tasks generated (/tasks command)
- [ ] Phase 4: Implementation complete
- [ ] Phase 5: Validation passed

**Gate Status**:
- [x] Initial Constitution Check: PASS
- [x] Post-Design Constitution Check: PASS
- [x] All NEEDS CLARIFICATION resolved
- [ ] Complexity deviations documented

---
*Based on Constitution v2.1.1 - See `/memory/constitution.md`*