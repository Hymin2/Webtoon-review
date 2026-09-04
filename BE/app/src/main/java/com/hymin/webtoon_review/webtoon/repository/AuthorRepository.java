package com.hymin.webtoon_review.webtoon.repository;

import com.hymin.webtoon_review.webtoon.entity.Author;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    @Query(value = "select a from Author a inner join WebtoonAuthor wa on wa.author = a inner join Webtoon w on wa.webtoon = w where w.id =:id")
    List<Author> findByWebtoonId(@Param("id") Long id);
}
