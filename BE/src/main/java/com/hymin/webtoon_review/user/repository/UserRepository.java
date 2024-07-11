package com.hymin.webtoon_review.user.repository;

import com.hymin.webtoon_review.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsByUsernameAndPassword(String username, String password);
}
