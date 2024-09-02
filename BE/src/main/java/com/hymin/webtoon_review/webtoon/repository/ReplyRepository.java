package com.hymin.webtoon_review.webtoon.repository;

import com.hymin.webtoon_review.webtoon.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {

}
