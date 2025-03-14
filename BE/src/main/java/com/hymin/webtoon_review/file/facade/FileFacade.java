package com.hymin.webtoon_review.file.facade;

import com.hymin.webtoon_review.file.dto.FileRequest.FileUploadInfo;
import com.hymin.webtoon_review.file.service.FileService;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileFacade {

    private final FileService fileService;

    public String startUpload(FileUploadInfo fileUploadInfo) {
        return fileService.startUpload(fileUploadInfo);
    }

    public String getFileUrl(String fileName) {
        return fileService.getFileUrl(fileName);
    }

    public Integer getUploadProgress(String fileName) {
        return fileService.getUploadProgress(fileName);
    }

    public void upload(InputStream inputStream, String fileName) {
        fileService.upload(inputStream, fileName);
    }

    public void upload(InputStream inputStream, String fileName, int chunkSize, int chunk) {
        fileService.upload(inputStream, fileName, chunkSize, chunk);
        fileService.updateFileChunkInfo(fileName);
    }
}
