package com.hymin.chattest.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import com.hymin.chattest.hashing.DistributionResult;
import com.hymin.chattest.hashing.HashRoutingSimulator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("chat-hashing")
class ChatHashDistributionScenarioTest {

    private static final List<Integer> ROOM_COUNTS = List.of(1_000, 10_000, 100_000);
    private static final List<Integer> VIRTUAL_NODE_COUNTS = List.of(1, 10, 100, 500);
    private static final List<String> WORKERS = List.of("worker-1", "worker-2", "worker-3");

    @Test
    @DisplayName("채팅방 수와 가상 노드 수에 따른 작업 분배를 비교한다")
    void 채팅방_수와_가상_노드_수에_따른_작업_분배를_비교한다() {
        System.out.println("[채팅방 해시 분배 비교]");
        System.out.printf(
            "%-10s %-20s %-34s %12s %14s %14s%n",
            "채팅방 수", "방식", "워커별 배정 수", "표준편차", "변동계수(%)", "최대편차율(%)"
        );

        for (int roomCount : ROOM_COUNTS) {
            printAndVerify(
                roomCount,
                "Modulo",
                HashRoutingSimulator.simulateModulo(roomCount, WORKERS)
            );

            for (int virtualNodeCount : VIRTUAL_NODE_COUNTS) {
                printAndVerify(
                    roomCount,
                    "Consistent(K=" + virtualNodeCount + ")",
                    HashRoutingSimulator.simulateConsistentHash(
                        roomCount,
                        WORKERS,
                        virtualNodeCount
                    )
                );
            }
        }
    }

    private void printAndVerify(
        int roomCount,
        String method,
        DistributionResult result
    ) {
        assertThat(result.totalAssignments()).isEqualTo(roomCount);
        assertThat(result.assignments()).containsOnlyKeys(WORKERS);

        System.out.printf(
            "%,10d %-20s %-34s %,12.2f %,14.2f %,14.2f%n",
            roomCount,
            method,
            result.assignments().values(),
            result.standardDeviation(),
            result.coefficientOfVariationPercent(),
            result.maximumDeviationPercent()
        );
    }
}
