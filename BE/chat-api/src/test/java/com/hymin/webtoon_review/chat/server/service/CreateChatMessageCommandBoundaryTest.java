package com.hymin.webtoon_review.chat.server.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CreateChatMessageCommandBoundaryTest {

    private static final Set<String> FORBIDDEN_DEPENDENCY_FRAGMENTS = Set.of(
        "stomp",
        "websocket",
        "redis",
        "ChatMessageRoutingService",
        "ChatMessageDto"
    );

    @Test
    void commandServiceHasNoRealtimeOrWorkerRoutingDependency() {
        Set<String> dependencyNames = Arrays.stream(
                CreateChatMessageCommandService.class.getDeclaredFields()
            )
            .map(Field::getType)
            .map(Class::getName)
            .collect(Collectors.toSet());

        assertThat(dependencyNames).noneMatch(name ->
            FORBIDDEN_DEPENDENCY_FRAGMENTS.stream().anyMatch(name::contains)
        );
    }
}
