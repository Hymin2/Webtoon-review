package com.hymin.chattest.hashing;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class HashRoutingSimulator {

    private HashRoutingSimulator() {
    }

    public static DistributionResult simulateModulo(int roomCount, List<String> workers) {
        return simulate(roomCount, workers, roomId -> {
            long hash = Hashing.murmur3_128().hashLong(roomId).asLong();
            return workers.get(Math.floorMod(hash, workers.size()));
        });
    }

    public static DistributionResult simulateConsistentHash(
        int roomCount,
        List<String> workers,
        int virtualNodeCount
    ) {
        TreeMap<Long, String> ring = createRing(workers, virtualNodeCount);
        return simulate(roomCount, workers, roomId -> findWorker(ring, roomId));
    }

    private static DistributionResult simulate(
        int roomCount,
        List<String> workers,
        RoomRouter router
    ) {
        Map<String, Integer> assignments = new LinkedHashMap<>();
        workers.forEach(worker -> assignments.put(worker, 0));

        for (long roomId = 1; roomId <= roomCount; roomId++) {
            assignments.compute(router.route(roomId), (worker, count) -> count + 1);
        }

        return DistributionResult.from(roomCount, assignments);
    }

    private static TreeMap<Long, String> createRing(
        List<String> workers,
        int virtualNodeCount
    ) {
        TreeMap<Long, String> ring = new TreeMap<>();
        for (String worker : workers) {
            for (int index = 1; index <= virtualNodeCount; index++) {
                long hash = Hashing.murmur3_128()
                    .hashString(worker + ":" + index, StandardCharsets.UTF_8)
                    .asLong();
                ring.put(hash, worker);
            }
        }
        return ring;
    }

    private static String findWorker(TreeMap<Long, String> ring, long roomId) {
        long hash = Hashing.murmur3_128().hashLong(roomId).asLong();
        Map.Entry<Long, String> entry = ring.ceilingEntry(hash);
        return entry != null ? entry.getValue() : ring.firstEntry().getValue();
    }

    @FunctionalInterface
    private interface RoomRouter {

        String route(long roomId);
    }
}
