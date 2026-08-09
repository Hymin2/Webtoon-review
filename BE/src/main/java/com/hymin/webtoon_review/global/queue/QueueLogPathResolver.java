package com.hymin.webtoon_review.global.queue;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class QueueLogPathResolver {

    private final Path rootDirectory;

    public QueueLogPathResolver(@Value("${queue.log.directory:./logs}") String rootDirectory) {
        this.rootDirectory = Paths.get(rootDirectory).toAbsolutePath().normalize();
    }

    public Path rootDirectory() {
        return rootDirectory;
    }

    public Path resolve(FileName fileName) {
        return rootDirectory.resolve(fileName.getDirectory())
                .resolve(fileName.getName())
                .normalize();
    }

    public Path resolve(FileName fileName, String topic) {
        return rootDirectory.resolve(fileName.getDirectory())
                .resolve(fileName.getName(topic))
                .normalize();
    }
}
