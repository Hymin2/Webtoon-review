package com.hymin.webtoon_review.webtoon.repository;

import com.hymin.webtoon_review.webtoon.entity.Genre;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

    @Query(value = "select g from Genre g inner join WebtoonGenre wg on wg.genre = g inner join Webtoon w on wg.webtoon = w where w.id =:id")
    List<Genre> findByWebtoonId(@Param("id") Long id);
}
