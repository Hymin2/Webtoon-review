package com.hymin.webtoon_review.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class FileRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileUploadInfo {

        private Long userId;
        private String type;
        private String fileName;
        private Integer fileSize;
        private Integer chunkSize;
    }
}
