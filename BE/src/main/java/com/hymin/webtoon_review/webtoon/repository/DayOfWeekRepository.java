package com.hymin.webtoon_review.webtoon.repository;

import com.hymin.webtoon_review.webtoon.entity.DayOfWeek;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DayOfWeekRepository extends JpaRepository<DayOfWeek, Long> {

    @Query(value = "select dow from DayOfWeek dow inner join WebtoonDayOfWeek wdow on wdow.dayOfWeek = dow inner join Webtoon w on wdow.webtoon = w where w.id =:id")
    List<DayOfWeek> findByWebtoonId(@Param("id") Long id);
}
