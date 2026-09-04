package com.hymin.webtoon_review.global.queue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueLogArchiver {

    private final QueueLogLockManager queueLogLockManager;
    private final QueueLogPathResolver queueLogPathResolver;

    public void archive(String topic, int size) {
        Path logPath = queueLogPathResolver.resolve(FileName.QUEUE_DATE_FILE_NAME, topic);
        Path tempLogPath = queueLogPathResolver.resolve(FileName.TEMP_DATA_FILE_NAME, topic);
        Path offlineLogPath = queueLogPathResolver.resolve(FileName.OFFLINE_LOG_FILE_NAME, topic);

        File tempParent = tempLogPath.getParent().toFile();
        File offlineParent = offlineLogPath.getParent().toFile();

        if (!tempParent.exists()) {
            tempParent.mkdirs();
        }

        if (!offlineParent.exists()) {
            offlineParent.mkdirs();
        }
        
        if (queueLogLockManager.acquireLock(topic)) {
            try (BufferedReader reader = Files.newBufferedReader(logPath);
                BufferedWriter tempLogWriter = Files.newBufferedWriter(tempLogPath);
                BufferedWriter offlineWriter = Files.newBufferedWriter(offlineLogPath);) {
                String line;
                int count = 0;

                while ((line = reader.readLine()) != null) {
                    if (count < size) {
                        offlineWriter.write(line);
                        offlineWriter.newLine();
                    } else {
                        tempLogWriter.write(line);
                        tempLogWriter.newLine();
                    }
                    count++;
                }

                Files.move(tempLogPath, logPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                try {
                    Files.deleteIfExists(tempLogPath);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                throw new RuntimeException(e);
            } finally {
                queueLogLockManager.releaseLock(topic);
            }
        }
    }
}
