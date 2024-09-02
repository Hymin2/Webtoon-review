package com.hymin.webtoon_review.webtoon.repository;

import com.hymin.webtoon_review.webtoon.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

}
