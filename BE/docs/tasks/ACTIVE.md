# Active Tasks

| Task | Status | Goal |
|---|---|---|
| [TASK-001: chat-connection 책임 경계 분리](active/TASK-001-chat-connection-boundary.md) | Active | 기존 동작을 유지하면서 WebSocket/STOMP 연결, session/subscription lifecycle, Redis Pub/Sub 구독 및 Redis-to-STOMP bridge를 첫 물리 모듈 경계로 분리한다. |

현재 구현 작업은 시작하지 않았다. TASK-001은 클래스 이동과 build/run 경계 구성만 다루며 내부 로직 개선과 메시지 처리 구조 변경은 포함하지 않는다.
