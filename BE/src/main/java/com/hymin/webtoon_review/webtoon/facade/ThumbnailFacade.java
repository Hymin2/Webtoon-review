package com.hymin.webtoon_review.webtoon.facade;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ThumbnailFacade {

    @Value("${path.thumbnail}")
    private String thumbnailPath;

    public byte[] getThumbnail(String name) {
        try {
            Path path = Paths.get(thumbnailPath, name);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
