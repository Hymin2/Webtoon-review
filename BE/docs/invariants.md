# Chat System Invariants

## 1. 문서 목적

이 문서는 채팅 시스템을 현재 구조에서 목표 구조로 단계적으로 전환하는 동안 구현 방식과 무관하게 유지해야 할 correctness, consistency, recovery 계약을 정의한다. 구현자는 설계와 변경이 각 invariant를 보존하는지 확인하고, 리뷰어와 테스트는 `Verification`을 최소 검증 기준으로 사용한다.

`Always-on Invariants`는 모든 migration 단계에 적용한다. `Target-stage Invariants`는 명시한 기능 또는 계약이 활성화된 이후부터 적용하며, 그 전 단계의 현재 구현 사실로 해석하지 않는다. 이 문서는 저장소, broker, 식별자 생성법, fan-out 방식처럼 아직 결정되지 않은 기술 선택을 확정하지 않는다.

## 2. Always-on Invariants

### INV-001. 재시도는 하나의 logical message로 수렴한다

**Invariant**

- 동일 room, sender, client idempotency key로 수행한 순차·동시 재시도는 durable history에서 최대 하나의 logical message로 수렴해야 한다.
- 동일 key에 다른 payload가 들어왔을 때의 구체적인 응답 정책은 미결정이지만, 서로 다른 두 메시지로 조용히 수락해서는 안 된다.

**Why**

- timeout, 응답 유실, consumer 재처리 때문에 발생하는 정상적인 재시도가 사용자에게 중복 메시지로 보이거나 중복 저장되는 것을 막기 위해서다.

**Verification**

- 동일 key의 순차 재시도, 동시 요청, 응답 유실 후 재시도를 주입하고 authoritative storage의 logical message 수와 반환된 message identity가 하나로 수렴하는지 확인한다.
- 같은 key와 다른 payload를 보내도 두 개의 durable message가 생성되지 않는지 확인한다.

### INV-002. Durable data와 ephemeral data의 권한을 구분한다

**Invariant**

- 영속화된 메시지 본문과 message identity의 최종 근거는 authoritative message storage여야 한다.
- 캐시, Pub/Sub, session/presence, subscription 및 routing 상태는 durable message history나 authoritative membership의 Source of Truth로 사용해서는 안 된다.
- 파생 상태와 authoritative 상태가 충돌하면 authoritative 상태를 기준으로 판단하거나 파생 상태를 무효화해야 한다.

**Why**

- Redis 장애, TTL 만료, eviction, 재시작 또는 stale session이 데이터 유실이나 권한 상승으로 해석되는 것을 막기 위해서다.

**Verification**

- 캐시와 session/presence 데이터를 삭제하거나 stale 값으로 만든 뒤에도 durable message와 membership의 최종 판정이 변하지 않는지 확인한다.
- 파생 데이터만으로 존재하지 않는 durable message나 membership을 만들어내지 않는지 확인한다.

### INV-003. 메시지 송신과 조회는 authoritative membership으로 인가한다

**Invariant**

- 인증된 사용자라도 authoritative membership에 따라 해당 room의 유효한 member가 아니면 메시지를 송신하거나 durable history를 조회할 수 없어야 한다.
- Redis participant cache, online 상태, session 위치 또는 subscription 상태만으로 송신·조회 권한을 부여해서는 안 된다.

**Why**

- 연결 여부와 authoritative membership이 나타내는 참여 권한은 서로 다른 상태이며, ephemeral 상태를 권한 근거로 사용하면 stale 데이터가 정보 노출이나 무단 송신으로 이어질 수 있기 때문이다.

**Verification**

- 비회원 및 탈퇴한 사용자의 송신·조회 요청이 거부되는지 확인한다.
- Redis에 member처럼 보이는 stale participant/session 값을 넣어도 authoritative membership 판정이 이를 덮어쓰는지 확인한다.

### INV-004. Realtime Push는 비권위적이며 best-effort다

**Invariant**

- Realtime Push는 중복, 유실, 지연 또는 순서 역전이 발생할 수 있는 전달 신호로 취급해야 한다.
- Push 성공을 durable persistence의 증거로, Push 유실을 durable message 유실의 증거로 사용해서는 안 된다.
- Push 재처리나 중복 전달이 durable logical message를 추가로 만들거나 authoritative 내용을 변경해서는 안 된다.

**Why**

- 현재와 목표 구조 모두 실시간 경로에 재연결, consumer 재처리, Pub/Sub 단절과 WebSocket 장애가 존재하며 end-to-end exactly-once 전달을 제공하지 않기 때문이다.

**Verification**

- 동일 메시지의 Push를 중복·지연·누락·역순으로 전달하고 durable message 수와 내용이 변하지 않는지 확인한다.
- Push 경로를 차단해도 이미 영속화된 메시지를 authoritative 조회에서 확인할 수 있는지 검사한다.

### INV-005. Ordering의 범위를 넘겨 해석하지 않는다

**Invariant**

- canonical ordering 값 또는 DB 정렬 규칙이 제공되는 경우, 표시·pagination·복구의 기준은 그 값 또는 규칙이어야 하며 Push 도착 순서를 canonical order로 사용해서는 안 된다.
- 시스템은 명시적으로 정의된 범위를 넘어 전역 ordering을 보장한다고 가정해서는 안 된다.
- room sequence 유지 여부와 구현 방식이 바뀌더라도, 노출된 ordering/cursor 계약과 그에 의존하는 조회·읽음·복구 동작은 함께 일관되게 전환되어야 한다.

**Why**

- worker topology 변경, retry, relay concurrency와 네트워크 지연은 실시간 도착 순서를 바꿀 수 있고, 현재도 전역 순서는 정의되어 있지 않기 때문이다.

**Verification**

- 서로 다른 room 및 같은 room의 이벤트를 역순·지연 전달해도 결과가 정의된 canonical 규칙대로 정렬되는지 확인한다.
- ordering 표현을 변경하는 migration에서 pagination, 읽음 위치와 gap/catch-up 테스트가 함께 통과하는지 확인한다.

## 3. Target-stage Invariants

### TASK-004 적용 상태 (2026-09-04)

- 새 `POST /chat/room/{roomId}/messages` 경로에 한해 INV-001~003, INV-005~007과 INV-012의 command transaction 범위가 구현·검증됐다.
- INV-001의 same-key/different-payload 상세 conflict 판정은 구현하지 않았지만 두 번째 durable message/outbox는 생성하지 않는다.
- INV-006 성공은 MongoDB의 `RoomSequence + ChatMessage + MessageCreated Outbox` commit 또는 같은 key의 기존 durable 결과 확인을 뜻한다.
- INV-007은 outbox record 원자 생성까지만 활성화됐다. Relay가 없는 상태이므로 INV-008은 아직 활성화되지 않았다.
- INV-009와 realtime/client catch-up은 아직 활성화되지 않았다. 새 HTTP command는 기존 STOMP/Redis realtime path에 연결되지 않는다.
- 이 적용 상태는 legacy/new writer cutover, 기존 room sequence migration 또는 safe coexistence 완료를 뜻하지 않는다.

### INV-006. 수락 성공은 authoritative persistence 완료를 뜻한다

적용 시점: migration 단계 2의 persistence-first command 경로가 활성화된 이후.

**Invariant**

- 성공으로 수락한 메시지는 응답 시점에 authoritative message storage에 commit되어 있어야 한다.
- 영속화되지 않았거나 commit 여부를 확인하지 못한 메시지를 성공으로 응답해서는 안 된다.
- durable broker 발행 성공이나 Realtime Push 성공은 송신 성공의 필요조건도 대체조건도 아니다.

**Why**

- 사용자에게 보인 성공과 복구 가능한 durable 상태를 일치시키고, Push-only 또는 broker-only 메시지가 생기는 것을 막기 위해서다.

**Verification**

- DB commit 실패·rollback·timeout을 주입했을 때 성공 응답이 나오지 않는지 확인한다.
- message와 적용 가능한 publication obligation의 authoritative commit 후 broker와 Push를 중단해도 송신 성공 응답과 authoritative 조회가 가능하고, 반대로 broker/Push만 성공했을 때는 송신 성공이 되지 않는지 확인한다.

### INV-007. Message와 event publication obligation은 원자적이다

적용 시점: migration 단계 2의 Transactional Outbox 또는 동등한 원자적 publication-obligation 패턴이 활성화된 이후.

**Invariant**

- 수락 대상 message와 그 message에 필요한 event publication 상태는 하나의 원자적 commit 단위로 함께 생성되거나 함께 rollback되어야 한다.
- commit되지 않은 message는 후속 실시간 event의 원본이 되어서는 안 되며, commit된 message에 필요한 publication obligation이 누락되어서는 안 된다.
- DB commit 뒤 broker에 직접 쓰는 별도 dual write를 correctness 근거로 사용해서는 안 된다.

**Why**

- DB에는 메시지가 있지만 event가 없거나, event는 전달됐지만 DB 메시지는 없는 partial failure를 제거하기 위해서다.

**Verification**

- message 기록과 publication 상태 기록 사이의 모든 failure point에서 프로세스를 종료하고 둘이 함께 존재하거나 함께 부재하는지 확인한다.
- commit 직후 프로세스를 종료해도 재시작 후 committed publication obligation이 발견되는지 검사한다.

### INV-008. Committed event는 재시도 가능하고 중복에 안전하다

적용 시점: migration 단계 2의 outbox relay 경로가 활성화된 이후.

**Invariant**

- committed publication obligation은 발행 완료가 확인되거나 명시된 복구 상태로 보존될 때까지 재시도 가능해야 하며, relay 장애 때문에 조용히 유실되어서는 안 된다.
- 발행 완료 표시 전후의 장애로 동일 event나 message가 중복 전달될 수 있음을 허용하고, consumer와 client는 그 중복을 안전하게 병합해야 한다.
- event transport 재생이나 consumer 재처리가 새로운 logical message를 생성해서는 안 된다.

**Why**

- commit, broker publish와 완료 표시 사이에는 원자적 경계가 없으므로 at-least-once와 idempotency가 복구 가능한 현실적 계약이기 때문이다.

**Verification**

- relay 중단, publish 성공 직후 종료, claim/lease 만료와 consumer 재처리를 주입하고 최종 발행은 이루어지되 durable logical message와 client 반영은 하나로 수렴하는지 확인한다.
- 장기 pending publication이 관측 가능하고 재처리 또는 보존 상태로 남는지 확인한다.

### INV-009. 클라이언트는 authoritative 조회로 최종 수렴한다

적용 시점: migration 단계 3의 DB 기반 catch-up 계약이 활성화된 이후.

**Invariant**

- 클라이언트는 재연결, gap 또는 의심스러운 상태에서 마지막으로 확정한 cursor 이후를 authoritative storage 기반 조회로 복구할 수 있어야 한다.
- 조회 결과와 Push는 stable message identity로 멱등 병합되어야 하며, 중복·누락·순서 역전 후에도 계약이 정한 canonical 상태로 최종 수렴해야 한다.
- 캐시가 비었거나 불완전하거나 coverage를 증명할 수 없을 때 이를 "메시지 없음"으로 확정해서는 안 되며 authoritative 조회로 fallback해야 한다.

**Why**

- best-effort Push와 선택적 캐시만으로는 누락 없는 이력을 보장할 수 없고, 실시간 경로 장애를 사용자 데이터 유실 없이 복구해야 하기 때문이다.

**Verification**

- Pub/Sub 유실, cache eviction/부분 손상, WebSocket 재연결, Push와 catch-up의 경계 중복을 주입하고 클라이언트 결과가 authoritative DB 결과와 일치하는지 확인한다.
- 여러 page의 catch-up을 중단·재개해도 마지막 확정 cursor부터 중복 없이 수렴하는지 확인한다.

### INV-010. Room subscription은 별도로 인가한다

적용 시점: room subscription authorization 계약이 도입된 이후이며, 늦어도 권한 검증을 Connection Server에 위임하는 fan-out 경로를 활성화하기 전.

**Invariant**

- 연결 인증만으로 room 구독 권한을 부여해서는 안 되며, 각 room subscription은 authoritative membership 계약에 따라 인가해야 한다.
- 권한 검증에 실패한 session은 해당 room의 local subscription이나 실시간 메시지를 얻어서는 안 된다.
- Redis channel subscription, room reference count, online/presence 상태는 사용자 구독 권한의 근거가 되어서는 안 된다.

**Why**

- 서버 또는 room 단위 fan-out에서는 stale local subscription이 권한 없는 사용자에게 메시지를 노출하는 correctness/security 문제가 될 수 있기 때문이다.

**Verification**

- 인증된 비회원이 room을 SUBSCRIBE할 때 거부되고 local/Redis subscription 상태가 권한을 우회해 생성되지 않는지 확인한다.
- stale room channel 및 presence 상태가 있어도 비회원 session으로 메시지가 전달되지 않는지 검사한다.

### INV-011. Session과 subscription 상태는 연결 생명주기에 수렴한다

적용 시점: 목표 session/presence lifecycle 계약이 활성화된 이후.

**Invariant**

- session 위치, online/presence와 routing subscription 같은 ephemeral 상태는 실제 연결 및 local subscription 생명주기에 맞게 생성·갱신·정리되어야 한다.
- 비정상 종료나 disconnect 누락으로 생긴 stale 상태가 영구히 남아서는 안 되며, eventual cleanup 후 살아 있는 연결과 subscription 상태로 수렴해야 한다.
- stale ephemeral 상태는 durable message나 membership을 변경하거나 권한을 부여해서는 안 된다.

**Why**

- stale routing은 불필요한 전송과 누락을 만들 수 있고, stale local subscription은 권한 문제로 이어질 수 있기 때문이다.

**Verification**

- 정상 disconnect, heartbeat timeout, Connection Server 강제 종료와 재시작을 주입하고 설정된 TTL·heartbeat·lifecycle 정책이 정의한 계약 범위 내에서 stale 상태가 정리되는지 확인한다. 구체적인 시간 값과 cleanup 구현은 이 invariant에서 확정하지 않는다.
- 정리 전후에도 durable history와 membership 판정이 변하지 않는지 검사한다.

### INV-012. Partial failure는 거짓 성공이나 조용한 유실을 만들지 않는다

적용 시점: migration 단계 2 이후에는 command와 outbox에, 단계 3 이후에는 catch-up을 포함한 전체 목표 복구 경로에 적용.

**Invariant**

- dependency 장애나 timeout이 발생해도 해당 단계의 성공 조건을 충족하지 않은 작업을 성공으로 표시해서는 안 된다.
- commit 성공 후 응답 유실처럼 결과가 불명확한 경우 동일 idempotency key 재시도 또는 authoritative 조회로 확정할 수 있어야 한다.
- message persistence, publication obligation/outbox 및 durable broker 처리처럼 복구가 필요한 durable work는 재시도, pending 또는 운영 가능한 recoverable failure state 중 하나로 남아야 하며, 단순 ACK나 폐기로 조용히 사라져서는 안 된다.
- Redis Pub/Sub과 WebSocket Push 같은 best-effort realtime delivery는 유실될 수 있으며, 각 실패를 durable pending/failure record로 남길 의무는 없다. 필요한 관측은 metric 등으로 수행하고, 최종 상태 복구는 활성화된 authoritative DB 기반 catch-up 계약을 따른다.
- 가용성 저하 시 요청을 거부하거나 제한하는 정책은 선택할 수 있지만, 영속화되지 않은 메시지를 임시 성공 처리해서는 안 된다.

**Why**

- Durable 처리와 best-effort realtime delivery는 독립적으로 실패할 수 있으므로, durable work의 복구 가능성을 보존하면서 realtime 실패는 authoritative 조회 기반 복구와 구분해야 하기 때문이다.

**Verification**

- message persistence, outbox/relay 및 durable broker 처리 장애를 각각 및 조합으로 주입하고 성공 응답, durable state, pending/recoverable failure state와 복구 결과가 모순되지 않는지 확인한다.
- Redis Pub/Sub과 WebSocket Push 장애를 주입해도 durable failure record 생성을 요구하지 않으며, 실패가 관측 가능하고 INV-009가 활성화된 이후에는 authoritative DB 기반 catch-up으로 최종 수렴하는지 확인한다.
- commit 후 응답을 유실시킨 뒤 동일 key 재시도 또는 조회가 기존 message identity를 반환하는지 검사한다.
