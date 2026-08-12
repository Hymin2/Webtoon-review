package com.hymin.chattest.hashing;

public record RedistributionResult(
    int totalRoomCount,
    int changedRoomCount,
    double redistributionPercent
) {

    static RedistributionResult from(int totalRoomCount, int changedRoomCount) {
        return new RedistributionResult(
            totalRoomCount,
            changedRoomCount,
            (double) changedRoomCount / totalRoomCount * 100.0
        );
    }
}
