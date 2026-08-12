package com.hymin.chattest.hashing;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record DistributionResult(
    Map<String, Integer> assignments,
    double average,
    double standardDeviation,
    double coefficientOfVariationPercent,
    double maximumDeviationPercent
) {

    static DistributionResult from(int roomCount, Map<String, Integer> assignments) {
        double average = (double) roomCount / assignments.size();
        double variance = assignments.values().stream()
            .mapToDouble(count -> Math.pow(count - average, 2))
            .average()
            .orElse(0.0);
        double standardDeviation = Math.sqrt(variance);
        double maximumDeviation = assignments.values().stream()
            .mapToDouble(count -> Math.abs(count - average))
            .max()
            .orElse(0.0);

        return new DistributionResult(
            Collections.unmodifiableMap(new LinkedHashMap<>(assignments)),
            average,
            standardDeviation,
            standardDeviation / average * 100.0,
            maximumDeviation / average * 100.0
        );
    }

    public int totalAssignments() {
        return assignments.values().stream().mapToInt(Integer::intValue).sum();
    }
}
