package com.hymin.webtoon_review.file.controller;

import com.hymin.webtoon_review.file.dto.FileRequest.FileUploadInfo;
import com.hymin.webtoon_review.file.service.FileService;
import com.hymin.webtoon_review.global.response.ApiResponse;
import com.hymin.webtoon_review.global.response.RestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    @GetMapping("/progress")
    public RestResponse getUploadProgress(@RequestParam("fileName") String fileName) {
        return ApiResponse.onSuccess(fileService.getUploadProgress(fileName));
    }

    @PostMapping("/start-upload")
    public RestResponse startUpload(@RequestBody FileUploadInfo fileUploadInfo) {
        return ApiResponse.onSuccess(fileService.startUpload(fileUploadInfo));
    }

    @PostMapping("/chunk")
    public RestResponse chunkUpload(@RequestParam("file") MultipartFile file,
        String fileName,
        int chunkSize,
        int chunk) {
        fileService.upload(file, fileName, chunkSize, chunk);

        return RestResponse.onCreated();
    }

    @PostMapping
    public RestResponse upload(@RequestParam("file") MultipartFile file) {
        fileService.upload(file);

        return RestResponse.onCreated();
    }
}
