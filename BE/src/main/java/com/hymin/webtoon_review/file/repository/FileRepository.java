package com.hymin.webtoon_review.file.repository;

import com.hymin.webtoon_review.file.entity.UploadFile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<UploadFile, Long> {

    Optional<UploadFile> findByName(String name);
}
