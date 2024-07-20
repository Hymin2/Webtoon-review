package com.hymin.webtoon_review.user.repository;

import com.hymin.webtoon_review.user.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {

}
