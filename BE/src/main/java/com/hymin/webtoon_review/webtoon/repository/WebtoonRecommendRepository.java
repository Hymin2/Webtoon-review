package com.hymin.webtoon_review.webtoon.repository;

import com.hymin.webtoon_review.user.entity.WebtoonRecommend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebtoonRecommendRepository extends JpaRepository<WebtoonRecommend, Long> {

}
