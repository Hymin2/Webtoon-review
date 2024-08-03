package com.hymin.webtoon_review.webtoon.repository;

import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebtoonRepository extends JpaRepository<Webtoon, Long>, WebtoonCustomRepository {

}
