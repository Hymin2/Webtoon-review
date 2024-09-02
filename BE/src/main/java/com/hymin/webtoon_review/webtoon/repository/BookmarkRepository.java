package com.hymin.webtoon_review.webtoon.repository;

import com.hymin.webtoon_review.user.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

}
