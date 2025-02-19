package com.hymin.webtoon_review.file.service;

import com.hymin.webtoon_review.file.dto.FileRequest.FileUploadInfo;
import com.hymin.webtoon_review.file.entity.UploadFile;
import com.hymin.webtoon_review.file.mapper.FileMapper;
import com.hymin.webtoon_review.file.repository.FileRepository;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${path.file}")
    private String filePath;
    private final FileRepository fileRepository;

    @PostConstruct
    public void createDirectory() {
        File dir = new File(filePath);

        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public Integer getUploadProgress(String fileName) {
        UploadFile uploadFile = fileRepository.findByName(fileName).orElse(null);

        return uploadFile == null ? 0 : uploadFile.getSavedChunk() + 1;
    }

    public String startUpload(FileUploadInfo fileUploadInfo) {
        String newFileName = makeFileName(fileUploadInfo.getFileName());
        fileRepository.save(FileMapper.toFile(fileUploadInfo, newFileName));

        return newFileName;
    }

    public void upload(MultipartFile file) {
        saveFile(file, makeFileName(file.getOriginalFilename()));
    }
    
    @Transactional
    public void upload(MultipartFile file, String fileName, int chunkSize, int chunk) {
        String chunkFileName = makeFileName(file.getOriginalFilename(), chunk);
        saveFile(file, chunkFileName);
        UploadFile uploadFile = fileRepository.findByName(fileName)
            .orElseThrow(RuntimeException::new);
        uploadFile.increaseSavedChunk();

        if (chunkSize == chunk) {
            String[] fileNameSplit = fileName.split("\\.");

            mergeFiles(fileNameSplit[0], fileNameSplit[1], chunkSize);
        }
    }

    private void mergeFiles(String originFileName, String extension, int chunkSize) {
        try {
            String fileName = originFileName + "_" + UUID.randomUUID() + "." + extension;
            Path path = Paths.get(filePath, fileName);
            Files.createFile(path);

            for (int chunk = 1; chunk <= chunkSize; chunk++) {
                String tempFileName = originFileName + "." + extension + ".part" + chunk;
                Path tempPath = Paths.get(filePath, tempFileName);

                Files.write(path, Files.readAllBytes(tempPath), StandardOpenOption.APPEND);
                Files.delete(tempPath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveFile(MultipartFile file, String fileName) {
        Path path = Paths.get(filePath, fileName);

        try {
            Files.write(path, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String makeFileName(String fileName) {
        String[] fileNameSplit = fileName.split("\\.");
        String newFileName = fileNameSplit[0] + "_" + UUID.randomUUID() + "." + fileNameSplit[1];

        return newFileName;
    }

    private String makeFileName(String fileName, Integer chunk) {
        String newFileName = fileName + ".part" + chunk;

        return newFileName;
    }
}
