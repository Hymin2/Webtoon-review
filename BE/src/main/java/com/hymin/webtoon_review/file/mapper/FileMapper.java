package com.hymin.webtoon_review.file.mapper;

import com.hymin.webtoon_review.file.dto.FileRequest.FileUploadInfo;
import com.hymin.webtoon_review.file.entity.UploadFile;

public class FileMapper {

    public static UploadFile toFile(FileUploadInfo fileUploadInfo, String newFileName) {
        return UploadFile.builder()
            .name(newFileName)
            .type(fileUploadInfo.getType())
            .savedChunk(0)
            .chunkSize(fileUploadInfo.getChunkSize())
            .isSaved(false)
            .build();
    }
}
