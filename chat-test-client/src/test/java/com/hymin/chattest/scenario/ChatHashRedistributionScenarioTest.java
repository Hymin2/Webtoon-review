package com.hymin.chattest.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import com.hymin.chattest.hashing.HashRoutingSimulator;
import com.hymin.chattest.hashing.RedistributionResult;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("chat-hashing")
class ChatHashRedistributionScenarioTest {

    private static final List<Integer> ROOM_COUNTS = List.of(1_000, 10_000, 100_000);
    private static final List<Integer> VIRTUAL_NODE_COUNTS = List.of(1, 10, 100, 500);
    private static final List<String> THREE_WORKERS = List.of(
        "worker-1", "worker-2", "worker-3"
    );
    private static final List<String> FOUR_WORKERS = List.of(
        "worker-1", "worker-2", "worker-3", "worker-4"
    );

    @Test
    @DisplayName("워커가 3개에서 4개로 확장될 때 채팅방 재배치율을 비교한다")
    void 워커_확장_시_채팅방_재배치율을_비교한다() {
        printRedistributionComparison("워커 확장 3개 → 4개", THREE_WORKERS, FOUR_WORKERS);
    }

    @Test
    @DisplayName("워커가 4개에서 3개로 축소될 때 채팅방 재배치율을 비교한다")
    void 워커_축소_시_채팅방_재배치율을_비교한다() {
        printRedistributionComparison("워커 축소 4개 → 3개", FOUR_WORKERS, THREE_WORKERS);
    }

    private void printRedistributionComparison(
        String title,
        List<String> beforeWorkers,
        List<String> afterWorkers
    ) {
        System.out.println("[" + title + "]");
        System.out.printf(
            "%-10s %-20s %18s %14s%n",
            "채팅방 수", "방식", "재배치 채팅방 수", "재배치율(%)"
        );

        for (int roomCount : ROOM_COUNTS) {
            printAndVerify(
                roomCount,
                "Modulo",
                HashRoutingSimulator.simulateModuloRedistribution(
                    roomCount,
                    beforeWorkers,
                    afterWorkers
                )
            );

            for (int virtualNodeCount : VIRTUAL_NODE_COUNTS) {
                printAndVerify(
                    roomCount,
                    "Consistent(K=" + virtualNodeCount + ")",
                    HashRoutingSimulator.simulateConsistentHashRedistribution(
                        roomCount,
                        beforeWorkers,
                        afterWorkers,
                        virtualNodeCount
                    )
                );
            }
        }
    }

    private void printAndVerify(
        int roomCount,
        String method,
        RedistributionResult result
    ) {
        assertThat(result.totalRoomCount()).isEqualTo(roomCount);
        assertThat(result.changedRoomCount()).isBetween(0, roomCount);

        System.out.printf(
            "%,10d %-20s %,18d %,14.2f%n",
            roomCount,
            method,
            result.changedRoomCount(),
            result.redistributionPercent()
        );
    }
}
