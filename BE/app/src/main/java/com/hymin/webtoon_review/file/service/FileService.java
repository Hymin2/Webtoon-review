package com.hymin.webtoon_review.file.service;

import com.hymin.webtoon_review.file.dto.FileRequest.FileUploadInfo;
import com.hymin.webtoon_review.file.entity.UploadFile;
import com.hymin.webtoon_review.file.exception.FileSizeLimitExceededException;
import com.hymin.webtoon_review.file.exception.UploadFileNotFoundException;
import com.hymin.webtoon_review.file.mapper.FileMapper;
import com.hymin.webtoon_review.file.repository.FileRepository;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${path.file}")
    private String filePath;
    private final FileRepository fileRepository;

    private static final Integer BUFFER_SIZE = 8 * 1024;
    private static final Integer MAX_FILE_SIZE = 1024 * 1024 * 100;

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

    public String getFileUrl(String fileName) {
        return "";
    }

    @Transactional
    public String startUpload(FileUploadInfo fileUploadInfo) {
        if (isGraterThanMaxFileSize(fileUploadInfo.getFileSize())) {
            throw new FileSizeLimitExceededException();
        }

        String newFileName = makeNewFileName(fileUploadInfo.getFileName());
        fileRepository.save(FileMapper.toFile(fileUploadInfo, newFileName));

        return newFileName;
    }

    public void upload(InputStream inputStream, String fileName) {
        saveFile(inputStream, fileName);
    }

    public void upload(InputStream inputStream, String fileName, int chunkSize, int chunk) {
        String chunkFileName = makeChunkFileName(fileName, chunk);
        saveFile(inputStream, chunkFileName);

        if (isLastChunk(chunk, chunkSize)) {
            String[] fileNameAndExtension = getFileNameAndExtension(fileName);

            mergeFiles(fileNameAndExtension[0], fileNameAndExtension[1], chunkSize);
        }
    }

    @Transactional
    public void updateFileChunkInfo(String fileName) {
        UploadFile uploadFile = fileRepository.findByName(fileName)
            .orElseThrow(UploadFileNotFoundException::new);
        uploadFile.increaseSavedChunk();
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

    private void saveFile(InputStream inputStream, String fileName) {
        Path path = Paths.get(filePath, fileName);

        try (OutputStream outputStream = new FileOutputStream(path.toFile())) {
            int read = 0;
            byte[] bytes = new byte[BUFFER_SIZE];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String makeNewFileName(String fileName) {
        String[] nameAndExtension = getFileNameAndExtension(fileName);
        String newFileName =
            nameAndExtension[0] + "_" + UUID.randomUUID() + "." + nameAndExtension[1];

        return newFileName;
    }

    private String makeChunkFileName(String fileName, Integer chunk) {
        String chunkFileName = fileName + ".part" + chunk;

        return chunkFileName;
    }

    private String[] getFileNameAndExtension(String fileName) {
        String[] result = new String[2];
        String[] fileNameSplit = fileName.split("\\.");

        String extension = fileNameSplit[fileNameSplit.length - 1];
        String originFileName = fileName.replace("." + extension, "");

        result[0] = originFileName;
        result[1] = fileNameSplit[fileNameSplit.length - 1];

        return result;
    }

    private Boolean isLastChunk(int chunk, int chunkSize) {
        return chunk == chunkSize;
    }

    private Boolean isGraterThanMaxFileSize(Integer fileSize) {
        return fileSize > MAX_FILE_SIZE;
    }
}
