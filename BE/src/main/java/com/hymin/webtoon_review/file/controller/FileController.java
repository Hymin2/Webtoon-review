package com.hymin.webtoon_review.file.controller;

import com.hymin.webtoon_review.file.dto.FileRequest.FileUploadInfo;
import com.hymin.webtoon_review.file.facade.FileFacade;
import com.hymin.webtoon_review.global.response.ApiResponse;
import com.hymin.webtoon_review.global.response.RestResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileFacade fileFacade;

    @GetMapping("/progress")
    public RestResponse getUploadProgress(@RequestParam("fileName") String fileName) {
        return ApiResponse.onSuccess(fileFacade.getUploadProgress(fileName));
    }

    @GetMapping("/url")
    public RestResponse getFileUrl(@RequestParam("fileName") String fileName) {
        return ApiResponse.onSuccess(fileFacade.getFileUrl(fileName));
    }

    @PostMapping("/start-upload")
    public RestResponse startUpload(@RequestBody FileUploadInfo fileUploadInfo) {
        return ApiResponse.onSuccess(fileFacade.startUpload(fileUploadInfo));
    }

    @PostMapping(value = "/chunk", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public RestResponse chunkUpload(HttpServletRequest request,
        @RequestParam("fileName") String fileName,
        @RequestParam("chunkSize") Integer chunkSize,
        @RequestParam("chunk") Integer chunk) throws IOException {
        fileFacade.upload(request.getInputStream(), fileName, chunkSize, chunk);

        return RestResponse.onCreated();
    }

    @PostMapping(consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public RestResponse upload(HttpServletRequest request,
        @RequestParam("fileName") String fileName) throws IOException {
        fileFacade.upload(request.getInputStream(), fileName);

        return RestResponse.onCreated();
    }
}
