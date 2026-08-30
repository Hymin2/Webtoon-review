# AGENTS.md

## Project

이 프로젝트는 기존 기능을 안정적으로 유지하면서
책임 분리, 유지보수성, 확장성, 안정성, 테스트 가능성을 중심으로 점진적으로 개선한다.
AI Agent는 코드를 바로 수정하기보다 현재 구조와 작업 목적을 먼저 이해한 뒤 작업한다.

## Documentation

작업 전 필요에 따라 다음 문서를 확인한다.

- `docs/architecture/`: 현재 및 목표 구조
- `docs/invariants.md`: 반드시 유지해야 하는 시스템 불변식
- `docs/tasks/ACTIVE.md`: 현재 진행 중인 작업
- `docs/tasks/active/`: 작업 상세 명세
- `docs/decisions/`: 주요 기술 및 설계 결정

## Task Workflow

1. 현재 active task 확인
2. 관련 문서와 코드 확인 및 호출 흐름 파악
3. Goal / In Scope / Out of Scope 확인
4. 변경 대상과 영향도 정리
5. 계획된 범위 내 구현
6. 테스트 및 검증 수행
7. 관련 문서 갱신

## Development Principles

- 한 task에서는 하나의 주요 관심사에 집중하며 점진적으로 변경한다.
- 가능한 한 단순한 해결책을 우선하며, 미래 확장성만을 위한 불필요한 추상화/계층/의존성을 추가하지 않는다.
- 성능 최적화는 측정된 병목을 근거로 수행한다.
- 기존 프로젝트의 코드 스타일과 규칙을 최우선으로 준수한다.

## Architecture & Module Principles

- 컴포넌트/모듈별 책임과 의존성 방향을 명확히 하고 비즈니스 로직과 인프라의 결합을 최소화한다.
- 데이터의 Source of Truth를 명확히 하고 저장 계층(DB, 캐시, MQ 등)의 역할을 엄격히 구분한다.
- 독립 배포/실행 요구가 없다면 MSA보다 모듈화를 우선한다.

## Distributed System Principles

분산 컴포넌트 연동 시 다음을 필수 검토한다.

- Duplicate processing & Idempotency (Exactly-once보다 멱등적 처리 우선)
- Ordering (순서 보장 범위 정의)
- Retry, Timeout, Partial failure & Dependency failure
- Data loss, Consistency & Failure recovery

## Implementation Rules

- Task의 In Scope만 수정하며, 범위 밖 이슈는 직접 수정하지 않고 `Findings`에 기록한다.
- 기존 구현과 추상화를 우선 재사용하며 기능 변경과 구조 변경은 가능한 한 분리한다.
- 코드, 설정, 문서 수정 시 UTF-8 인코딩을 유지하고 한글 깨짐이 없는지 확인한다.

## Testing & Review

전체 테스트보다 변경 범위 관련 테스트를 우선 실행한다.

1. 관련 테스트 메서드/클래스: `./gradlew test --tests "com.example.SomeTest.someTestMethod"`
2. 특정 모듈 관련 테스트: `./gradlew :module-name:test --tests "com.example.SomeTest"`
3. 특정 모듈 전체 테스트: `./gradlew :module-name:test`
4. 프로젝트 전체 회귀 검증이 필요한 경우: `./gradlew test`

프로젝트의 실제 모듈명과 테스트 경로를 확인한 뒤 명령을 실행한다.

테스트 통과를 위해 테스트를 임의 삭제/약화하지 않는다.
구현 후 Correctness, Regression, Concurrency, Transaction, Idempotency, Ordering, Failure recovery, Unnecessary complexity를 필수 검토한다.

상세 테스트 및 리뷰 절차는 `docs/prompts/`의 관련 문서를 따른다.

## Documentation Rules

- 구조/설계 변경 시 관련 문서를 갱신하고, 의미 있는 Trade-off는 `docs/decisions/`에 기록한다.
- 구조적 변경은 Mermaid 등 텍스트 기반 다이어그램을 활용한다.
- Task 완료 시 다음 항목을 기록한다:
  - **What Changed**: 변경 사항 및 Before/After (성능 비교는 실제 측정치가 있는 경우만 작성)
  - **Why**: 변경 이유
  - **Verification**: 검증 결과
  - **Findings**: 작업 중 발견한 범위 밖 이슈
  - **Remaining Risks**: 잔여 리스크

## Do Not

- 요청 없는 대규모 리팩터링이나 불필요한 외부 라이브러리 추가를 하지 않는다.
- 테스트 통과나 편의를 위해 예외 처리나 테스트를 약화/삭제하지 않는다.
- 확인되지 않은 가정을 사실처럼 구현하거나 문서화하지 않는다.
