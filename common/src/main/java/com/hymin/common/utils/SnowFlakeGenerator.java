package com.hymin.common.utils;

public class SnowFlakeGenerator implements PrimaryKeyGenerator{

    private static final long DEFAULT_EPOCH = 1700000000000L;

    private static final long NODE_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long maxNodeId = (1L << NODE_ID_BITS) - 1;
    private static final long maxSequence = (1L << SEQUENCE_BITS) - 1;

    private long nodeId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowFlakeGenerator(long nodeId) {
        if (nodeId > maxNodeId || nodeId < 0) {
            throw new IllegalArgumentException("잘못된 Node Id 입니다.");
        }

        this.nodeId = nodeId;
    }

    public synchronized long generate() {
        long timestamp = currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("잘못된 timestamp 입니다.");
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & maxSequence;

            if (sequence == 0) {
                timestamp = waitForNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return timestamp << (NODE_ID_BITS + SEQUENCE_BITS) | nodeId << SEQUENCE_BITS | sequence;
    }

    private long currentTimeMillis() {
        return System.currentTimeMillis() - DEFAULT_EPOCH;
    }

    private long waitForNextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }
}
