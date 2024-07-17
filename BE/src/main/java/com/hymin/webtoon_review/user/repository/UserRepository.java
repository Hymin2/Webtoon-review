package com.hymin.webtoon_review.user.repository;

import com.hymin.webtoon_review.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Boolean existsByUsernameOrNickname(String username, String nickname);

    Boolean existsByUsername(String username);

    Boolean existsByNickname(String nickname);
}
