# TASK-005: Gradle Module Boundary Cleanup

> 기존 root source를 `app` runtime module로 옮기고, 채팅방·membership 책임을 `chat-room` module로 분리하되 현재 실행 프로필과 채팅 동작을 바꾸지 않는다.

## Status

- 상태: Completed
- 기준 문서: `docs/architecture/current-chat.md`, `docs/architecture/target-chat.md`, `docs/invariants.md`
- 구현 상태: `app`/`chat-room` ownership 분리와 단방향 Gradle dependency, artifact 및 Compose wiring을 구현하고, Review 승인과 Test 검증을 완료했다.

## Summary

### Goal
- root project에 남아 있는 application source ownership을 `app` Gradle module로 이전하고, MySQL 기반 chat room/membership 책임을 `chat-room` module로 분리하여 root 전체를 향한 임시 의존성을 제거한다.

### In Scope
- root project를 source 없는 Gradle aggregator로 정리하고, 기존 root `src/main`, `src/test`, resources 및 persister runtime bootstrap을 `app` module로 이전한다.
- `ChatRoom`, `UserChatRoom`, room/membership repository·projection·service와 room REST orchestration을 `chat-room` module로 이전한다.
- 기존 mixed `ChatFacade`/`ChatRoomController`를 room 책임과 message command/query 책임으로 최소 분리하고, 기존 HTTP/STOMP mapping과 DTO/exception contract를 유지한다.
- `app`, `chat-room`, `chat-api`, `chat-connection`, `chat-dispatcher`의 단방향 Gradle dependency, boot/library artifact 및 Docker build/runtime target을 새 source ownership에 맞춘다.
- 이동한 테스트를 owning module로 옮기고, module build·artifact·compose configuration 및 기존 채팅 관련 테스트로 동작 보존을 검증한다.

### Out of Scope
- Kafka, Outbox Relay, outbox publish/claim/retry/cleanup, Kafka producer 또는 consumer 구현
- TASK-004의 MongoDB transaction, RoomSequence, durable idempotency, MessageCreated outbox schema/semantics 변경
- 기존 STOMP/Redis Stream/Pub/Sub routing, dispatcher fan-out, chat-persister algorithm, cache/session key 또는 profile 변경
- membership authorization 규칙, room API URL·HTTP status·DTO payload, JPA schema/query 변경
- `ChatService.connect()`/`disconnect()` 미사용 경로, STOMP subscription authorization, legacy/new writer cutover 또는 unrelated refactor

### Key Decisions
- root project는 Gradle aggregator만 남기며 application bootstrap, 기존 root runtime source/test/resources와 `chat-persister` executable responsibility는 `app`이 소유한다.
- `chat-room`은 `app`에만 의존한다. `app`은 `chat-room`을 참조하지 않으며, `chat-room`은 `chat-api`, `chat-connection`, `chat-dispatcher`에 의존하지 않는다.
- 목표 dependency DAG는 다음과 같다. 명시적인 direct dependency는 각 module이 compile에 실제 사용하는 Spring/library artifact에 한해 선언한다.

```text
root (aggregator)
app
chat-room -------> app
chat-api --------> app, chat-room
chat-connection -> app, chat-api
chat-dispatcher -> app, chat-room
```

- `ChatMapper`의 message DTO/entity mapping은 `app`에 유지한다. `ChatRecentMessageCacheService`와 dispatcher가 이를 사용하므로 전체 mapper를 `chat-room`으로 옮기지 않는다. room/member entity 및 room response mapping만 `chat-room`의 mapper로 분리한다.
- 현재 `ChatFacade`의 STOMP send 및 message-history query orchestration은 `chat-api`에 남기고, room list/info/join orchestration은 `chat-room`으로 옮긴다. `ChatRoomController`의 history endpoint는 query service를 사용하는 `chat-api` controller에 남겨 module 역의존을 만들지 않는다. URL, request/response, authorization check order와 transaction annotation semantics는 유지한다.
- executable artifact 역할은 유지한다: `app` boot jar는 `chat-persister`, `chat-connection` boot jar는 `chat`, `chat-dispatcher` boot jar는 `chat-worker` profile을 계속 실행한다. Dockerfile/Compose는 source·artifact 이름과 runtime target만 새 ownership에 맞게 변경한다.

관련 invariant:
- INV-001, INV-002, INV-003, INV-004, INV-005, INV-006, INV-007

관련 ADR:
- None

### Steps
1. `app` 및 `chat-room` subproject를 추가하고 root/app source, test, resource 및 executable artifact ownership을 Gradle/Docker/Compose에 반영한다.
2. chat room/membership entity, repository, projection, exception, service, room mapper와 room REST orchestration을 `chat-room`으로 이동하고 mixed API facade/controller를 message와 room 책임으로 분리한다.
3. 모든 caller import와 project dependency를 새 DAG로 갱신하고, 중복 source/bean/mapping 없이 기존 package scan, profile 및 endpoint contract가 유지되는지 확인한다.
4. owning module별 테스트와 build/artifact/compose 검증을 실행하고, dependency cycle·root source 잔존·동작 보존 결과를 기록한다.

### Acceptance Criteria

#### Correctness / Contract
- [ ] 기존 root application production/test/resource source는 `app` 또는 명시한 `chat-room` ownership으로 한 번만 존재하고 root project에는 production/test source가 남지 않는다.
- [ ] `ChatRoom`, `UserChatRoom`, room/member repository와 projection, room/member authorization service 및 room list/info/join REST orchestration은 `chat-room`에 있고, 기존 URL, HTTP status, DTO payload, JPA query/schema 및 roomMemberId 생성 동작이 유지된다.
- [ ] STOMP `SEND /pub/chat/messages`, REST message history 조회 및 TASK-004 `POST /chat/room/{roomId}/messages`의 controller/service contracts와 message/outbox transaction behavior가 유지된다.
- [ ] `chat-persister`, `chat`, `chat-worker` profile의 executable artifact와 Compose service role이 기존과 같다.

#### Consistency / Failure
- [ ] membership은 계속 MySQL `UserChatRoomRepository`의 authoritative result로 send/history/new HTTP command 전에 확인되며, 실패 시 Mongo transaction, Redis routing 또는 durable write를 시작하지 않는다.
- [ ] TASK-004 message + RoomSequence + outbox Mongo transaction, retry 및 commit-uncertainty behavior가 module move로 변경되지 않는다.

#### Boundary / Regression
- [ ] project dependency graph는 `chat-room -> app`, `chat-api -> app, chat-room`, `chat-connection -> app, chat-api`, `chat-dispatcher -> app, chat-room`만의 단방향 관계이며 root 또는 `app`에서 child module로 향하는 project dependency와 모든 cycle이 없다.
- [ ] `app`은 room module type를 import하지 않는다. `chat-room`은 `chat-api`, `chat-connection`, `chat-dispatcher` type를 import하거나 dependency로 갖지 않는다.
- [ ] message mapping을 쓰는 app cache/persister/routing 및 dispatcher는 `chat-room` mapper에 의존하지 않으며, room mapping은 `chat-room` ownership으로 한 번만 존재한다.
- [ ] Kafka/Relay와 범위 밖 realtime/persistence behavior 변경이 diff에 포함되지 않는다.

---

## Detail

### Current State
- root project가 `WebtoonReviewApplication`, global/user/webtoon/file/search, shared chat contract/message/cache/session/routing/persister, `application.yml`, `application-chat.yml`, `application-chat-persister.yml` 및 root boot jar를 소유한다. `chat-persister` Compose service는 그 root artifact를 실행한다.
- `chat-api`, `chat-connection`, `chat-dispatcher`는 각각 `implementation project(':')`로 root 전체에 의존한다. `chat-connection`은 추가로 `chat-api`를 의존한다.
- `ChatRoom`, `UserChatRoom`, `ChatRoomRepository`, `UserChatRoomRepository`와 projection은 root에 있지만, `ChatService`, `ChatFacade`, `ChatRoomController`는 `chat-api`에 섞여 있다. `UserChatRoomRepository`는 API의 room authorization 및 durable HTTP command뿐 아니라 dispatcher의 recipient lookup에도 직접 사용된다.
- `ChatFacade`는 STOMP send, room list/info/join, message history를 모두 처리한다. 그중 room list/info/join은 room/membership 책임이지만 send/history는 `ChatMessageRoutingService` 또는 `ChatMessageQueryService`에 의존해 `chat-api`에 남아야 한다.
- `ChatMapper`는 room/member mapping뿐 아니라 app `ChatRecentMessageCacheService`와 dispatcher의 message mapping에도 쓰인다. mapper 전체를 room module로 옮기면 `app -> chat-room -> app` cycle이 생긴다.

```text
Current
chat-connection -> chat-api -> root
chat-connection -> root
chat-dispatcher -> root

root: app feature + shared chat message/persister + room/member model/repository
chat-api: message API + mixed room facade/service/controller
```

### Target State
- `app` owns the former root runtime: bootstrap, non-room application feature, global infrastructure, shared chat message contract/entity/cache/session/routing/persister, and the persister boot jar.
- `chat-room` owns the MySQL room/membership model and APIs: `ChatRoom`, `UserChatRoom`, repositories/projections, room/member exception and service, room-only mapper, room list/info/join facade and controller, and their tests.
- `chat-api` retains message command/query transport and orchestration. Its message facade obtains membership from `chat-room`; its history controller remains separate from the room-only controller while preserving `/chat/room/{roomId}/messages`.
- `chat-dispatcher` uses `chat-room` only for authoritative participant lookup; `chat-connection` remains independent of `chat-room` except transitively through `chat-api` at runtime.

```text
STOMP send -> chat-api message facade -> chat-room membership check -> app routing
REST history -> chat-api query controller -> chat-room membership check -> app query/cache
Room list/info/join -> chat-room controller/facade -> app user/webtoon services + room repositories
Dispatcher recipient lookup -> chat-room repository
```

### Design Details

#### Source and artifact ownership
- Create `app` and `chat-room` in `settings.gradle`. Move the former root `src/main`/`src/test`/resources to `app` except the explicit chat-room classification below; root build configuration becomes aggregation only.
- Keep `WebtoonReviewApplication` in `app` under the existing base package so Spring component/entity/repository scanning continues to find types packaged in dependent modules. Do not add a second bootstrap class or alter active profile names.
- Move `ChatRoom`, `UserChatRoom`, `ChatRoomRepository`, `UserChatRoomRepository`, their projections, `ChatRoomNotFoundException`, `InvalidChatRoomAccessException`, and room/member unit tests to `chat-room`.
- Extract room-only mapping from the shared `ChatMapper` into `chat-room`. Keep message response/DTO/entity conversion in `app`, update API/dispatcher/cache callers accordingly, and do not duplicate mapping logic.
- Move the room/membership service from `chat-api` into `chat-room`. Split only the mixed orchestration necessary to keep dependency direction: room list/info/join facade/controller move to `chat-room`; STOMP send and message-history orchestration/controller remain in `chat-api` and use the moved membership service. Preserve existing public mappings instead of introducing versioned endpoints or ports/adapters.
- `app` produces the former root executable role for `chat-persister`; update Dockerfile builder copies, artifact copy paths and runtime target naming, then update Compose only to use that corresponding target. Retain dependency conditions, environment variables, service names and profile values.

#### Dependency boundaries
- `chat-room -> app` is required because its JPA entities and room facade use existing `BaseEntity`, `User`, `Webtoon`, shared responses/exceptions and application services. It must not pull message API or execution modules back into the domain boundary.
- `chat-api -> chat-room` is required for authoritative membership and room endpoints. It continues to depend on `app` for message model, query/cache/routing, global web/security/transaction configuration and user/webtoon services used by legacy message flow.
- `chat-dispatcher -> chat-room` is required solely by its existing participant repository lookup. It continues to depend on `app` for Redis, DTO, cache/session, metrics and utility types.
- `chat-connection` retains its direct `app` and `chat-api` dependencies. It does not receive a direct `chat-room` dependency because its existing connection lifecycle has no authoritative room lookup.
- Module build files declare only direct compile/test dependencies used by their own sources. No module relies on root's old `implementation` dependencies leaking transitively.

### Test Plan

#### Correctness
- Run moved room/member entity, mapper and service tests from `:chat-room:test`; run former root app tests from `:app:test`; run `:chat-api:test`, `:chat-connection:test` and `:chat-dispatcher:test`.
- Specifically verify existing tests covering roomMemberId generation, membership rejection, STOMP message-id/routing preparation, message query, dispatcher participant lookup and TASK-004 command/controller transaction boundaries from their new owners.
- Confirm no duplicate controller mapping or Spring bean is introduced and that the existing `chat` profile exposes the same room and message routes.

#### Integration
- Run `./gradlew :app:bootJar :chat-room:jar :chat-api:jar :chat-connection:bootJar :chat-dispatcher:bootJar` and inspect artifacts for one copy of moved production classes and required runtime dependencies.
- Run `./gradlew projects` plus each module's relevant `dependencies --configuration runtimeClasspath` report to confirm the target DAG and absence of root/child cycles.
- Run `docker compose config --quiet`; where Docker execution is available, build the app/connection/dispatcher targets and smoke each existing profile with its unchanged Compose configuration.

#### Failure / Recovery
- Re-run the TASK-004 focused unit/controller and replica-set integration tests where the required MongoDB/Docker prerequisite is available; verify membership failure still occurs before a Mongo transaction and existing retry/commit-uncertainty tests retain their result.
- Verify this Task neither adds a relay nor activates an outbox publication path; a durable HTTP command remains non-realtime until a separately approved Relay Task.

### Risks / Open Questions
- `ChatRoomController` currently owns both room endpoints and message history. The implementation must split the class at the existing method boundary without changing endpoint mappings or causing duplicate mapping registration.
- The repository's broader root context test has recorded environment-related MySQL/Loki failures in TASK-004. A failure must be classified against the existing evidence and not addressed by weakening tests or changing unrelated infrastructure.
- The actual production deployment topology outside Compose is not available; artifact filename/runtime target changes must be propagated to any external deployment pipeline separately if one exists.

### Findings
- `ChatService.connect()`/`disconnect()` and `UserChatRoom.isConnected` update paths are not called by production code according to the current architecture document. They move with membership ownership but are not corrected in this Task.
- STOMP SUBSCRIBE still lacks authoritative membership authorization. This is a separate target-stage invariant and is not changed by the module split.

### Follow-ups
- Design and implement a separate Outbox Relay Task (claim/lease, publish confirmation, retry, observability and duplicate-safe downstream behavior) after explicit approval.
- Revisit legacy STOMP/new HTTP writer coexistence, room sequence migration/cutover and realtime delivery only in separately scoped Tasks.

---

## Work Summary

### Implementation
- Root project is source-free and the former runtime source/test/resources now belong to `app`.
- Added `chat-room` for room/membership entities, repositories, projections, service, mapper and room REST facade/controller.
- Split message history and STOMP orchestration from room orchestration; message mapping remains in `app` and room mapping is owned by `chat-room`.
- Preserved the moved REST controllers' explicit parameter bindings and non-negative history-sequence validation.
- Updated module project dependencies and Docker builder/artifact paths for the new ownership.
- Removed the moved local Firebase service-account file; Docker now recursively excludes module resource credentials.
- Applied Spring Boot dependency management to the non-executable chat-room library and disabled its boot jar.
- Restored the STOMP message-receipt log and the repository's prior ignore policy, adding only multi-module generated-output rules.
- Declared `chat-room`'s direct Security dependency, removed the duplicate app QueryDSL processor, and restored message-id plus history authorization-order coverage.
- Replaced conflicting wildcard imports in `ChatRoomController` with explicit application response and Spring MVC imports, resolving the independent `chat-room` compilation ambiguity.

### Review
- Decision: ADVANCE
- Stage Result: APPROVED
- Critical: 0
- Important: 0
- Minor: 0
- Key Findings: None.
- Key Evidence: root `src` is absent; every former root source is present exactly once in `app` or `chat-room`; room mapping is owned only by `chat-room` while message mapping remains in `app`; no forbidden `app -> chat-room` import or reverse execution-module dependency was found; the declared project DAG, HTTP/STOMP mappings, membership-before-send/history checks, artifact targets, and Compose profile/service wiring match the Task contract.
- Resolved Previous Finding: `ChatRoomController` now uses explicit application-response and Spring MVC imports, so the prior `ResponseStatus` ambiguity is absent from the reviewed source.
- Verification Boundary: this Review is static; final fresh Gradle compilation, module tests, artifact inspection, and profile smoke verification remain the Test stage's responsibility.
- Next Action: Advance to Test and run the Task test/artifact plan in a Gradle-capable environment.
- Details: None

### Verification Summary
- Decision: REWORK
- Stage Result: FAIL

| Area | Result | Evidence |
|---|---|---|
| Correctness | FAIL | Fresh `:chat-room:compileJava` failed: `ChatRoomController.java:29` has an ambiguous `ResponseStatus` reference from wildcard imports of `global.response` and `org.springframework.web.bind.annotation`. |
| Module regression | Partial PASS | `./gradlew --no-daemon projects` succeeded and fresh `:app:test` ran 38 tests; 25 chat/shared tests passed. The 13 failures match the documented pre-existing MySQL/Loki/user-test baseline, but Gradle stopped before the remaining module tests because `chat-room` does not compile. |
| Integration | PASS | Fresh `docker compose -f compose.yml config --quiet` succeeded. Artifact verification cannot run until `chat-room` compiles. |
| Concurrency | Not run | No behavior change planned; compilation failure blocks final module tests. |
| Performance | Not run | No performance change planned. |

- Blocking Failure: `chat-room` cannot compile independently, violating the required owning-module build boundary.
- Next Action: In Implement, replace the conflicting wildcard imports in `ChatRoomController` with explicit imports (or fully qualify the application `ResponseStatus`), then rerun the focused module tests and artifact plan.
- Details: None

### Result
- Completed — The root project is source-free, application and room/membership ownership are separated into `app` and `chat-room`, and the approved dependency/artifact boundaries preserve the existing chat contracts and profiles. Focused module tests and required artifact/Compose verification passed.

### Remaining
- No TASK-005 implementation work remains. The documented broader `:app:test` baseline still contains 13 environment-related MySQL/Loki/user-test failures; Mongo replica-set integration tests were not rerun because the required service was unavailable. These are outside the verified focused evidence and do not change the module-boundary result.

---

## Work Log

| Date | Stage | Summary | Evidence / Result |
|---|---|---|---|
| 2026-09-04 | Plan | TASK-005 scope, dependency DAG and ownership split defined from current source callers and build/runtime configuration. | `AGENTS.md`, architecture/invariant docs, Gradle files, Dockerfile/Compose and production/test caller analysis |
| 2026-09-04 | Implement | Moved runtime ownership to `app`, extracted room/membership to `chat-room`, split room/message HTTP orchestration and updated module/Docker boundaries. | Static ownership/import checks passed; Gradle compile was blocked by environment restrictions. |
| 2026-09-04 | Implement | Restored the pre-move room/history controller binding and validation annotations after the module split. | Static controller comparison; Gradle compile remains unavailable because the wrapper distribution cannot be downloaded in this environment. |
| 2026-09-04 | Review | Independently reviewed TASK-005 contract, complete boundary/config diff, moved controllers/services/mappers, tests and runtime packaging. | CHANGES_REQUESTED / REWORK — Critical 1, Important 1, Minor 2; Gradle execution remained sandbox-blocked by daemon socket creation. |
| 2026-09-04 | Implement | Addressed review findings: removed local Firebase credentials, fixed module dependency management, restored the receipt log and preserved ignore policy. | Static credential/path and diff checks passed; narrow Gradle compilation attempted separately. |
| 2026-09-04 | Review | Re-reviewed the full TASK-005 boundary/config diff, split orchestration, moved source/tests and prior finding fixes. | CHANGES_REQUESTED / REWORK — Critical 0, Important 1, Minor 2; direct `chat-room` Security dependency remains missing. |
| 2026-09-04 | Implement | Addressed the latest module-boundary review findings. | Added direct Security ownership, removed duplicate QueryDSL processor, and restored message-id/history authorization tests; focused Gradle test attempted. |
| 2026-09-04 | Review | Re-reviewed TASK-005 after the latest boundary and test fixes against the full Task contract, invariants, source ownership, module DAG and runtime packaging. | APPROVED / ADVANCE — Critical 0, Important 0, Minor 0; static and Compose checks passed, while Gradle execution remains sandbox-limited for Test. |
| 2026-09-04 | Test | Ran fresh Gradle project, module-test and compile verification with a writable task-local Gradle cache, plus Compose config validation. | FAIL / REWORK — `:chat-room:compileJava` deterministically fails at `ChatRoomController.java:29` because `ResponseStatus` is ambiguous between application and Spring wildcard imports; Compose config passed. |
| 2026-09-04 | Implement | Addressed the Test compilation finding in the room REST controller. | Replaced the conflicting wildcard imports with explicit imports; focused Gradle compilation was attempted but the wrapper distribution download is blocked by this environment's network policy. |
| 2026-09-04 | Review | Independently re-reviewed TASK-005 after the controller import correction, including complete ownership, dependency, mapping, authorization-order, and runtime-wiring checks. | APPROVED / ADVANCE — Critical 0, Important 0, Minor 0; final executable verification remains in Test. |
| 2026-09-04 | Test | Performed fresh, scope-bounded module tests and runtime artifact verification after the approved review. | PASS / ADVANCE — 57 focused chat tests passed; required artifacts and Compose configuration were generated/validated. |
| 2026-09-04 | Document | Recorded verified module ownership and runtime boundaries in current architecture, finalized TASK-005, and removed it from the active registry. | Completed — architecture reflects `app`/`chat-room` ownership; focused tests, artifacts and Compose evidence support completion. |

### Detailed Log
- Test verification (fresh): `./gradlew --rerun-tasks :chat-room:test :chat-api:test` passed 34 tests covering room/member ownership, membership rejection, message command/query, controller, facade, message-id and transaction-boundary behavior.
- Test verification (fresh): `./gradlew --rerun-tasks :chat-connection:test :chat-dispatcher:test` passed 5 tests covering STOMP interception plus chat/worker profile and sequence behavior. The moved `app` chat/persister test subset passed 18 tests in this session.
- Artifact/runtime verification (fresh): `./gradlew --rerun-tasks :app:bootJar :chat-room:jar :chat-api:jar :chat-connection:bootJar :chat-dispatcher:bootJar` generated all required artifacts. The app boot jar contains `WebtoonReviewApplication`; the chat-room library jar contains `ChatRoom`, `UserChatRoom`, and `ChatRoomMapper`. `docker compose -f compose.yml config --quiet` passed.
- Boundary evidence: root `src` is absent; direct project dependencies match the approved DAG (`chat-room -> app`, `chat-api -> app, chat-room`, `chat-connection -> app, chat-api`, `chat-dispatcher -> app, chat-room`).
- Remaining risk: tagged Mongo replica-set integration tests were not run because this task does not alter that integration boundary and the required replica-set service is unavailable; their existing results must not be treated as fresh evidence for this stage.
