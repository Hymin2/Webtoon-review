package com.hymin.webtoon_review.global.queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueueLogWriterTest {

    @TempDir
    private Path tempDirectory;

    @Mock
    private JobQueue jobQueue;
    @Mock
    private QueueLogLockManager queueLogLockManager;

    @Test
    void initializesQueueLogsUnderConfiguredDirectory() throws Exception {
        when(jobQueue.getAllTopic()).thenReturn(List.of("chat-message"));
        QueueLogPathResolver pathResolver = new QueueLogPathResolver(tempDirectory.toString());
        QueueLogWriter queueLogWriter = new QueueLogWriter(
                jobQueue,
                queueLogLockManager,
                pathResolver
        );

        queueLogWriter.init();

        Path topicFile = tempDirectory.resolve("topics.txt");
        assertThat(topicFile).exists();
        assertThat(Files.readString(topicFile)).isEqualTo("chat-message\n");
    }
}
