package com.hymin.webtoon_review.webtoon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebtoonFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_star_score")
    private Integer totalStarScore;

    @Column(name = "man_star_score")
    private Integer manStarScore;

    @Column(name = "female_star_score")
    private Integer femaleStarScore;

    @Column(name = "total_popularity_score")
    private Integer totalPopularityScore;

    @Column(name = "man_popularity_score")
    private Integer manPopularityScore;

    @Column(name = "female_popularity_score")
    private Integer femalePopularityScore;

    @Column(name = "recommendation_count")
    private Integer recommendationCount;

    @Column(name = "webtoon_created_at", updatable = false)
    private LocalDateTime webtoonCreatedAt;

    @ManyToOne
    @JoinColumn(name = "webtoon_id")
    private Webtoon webtoon;

    @ManyToOne
    @JoinColumn(name = "day_of_week_id")
    private DayOfWeek dayOfWeek;

    @ManyToOne
    @JoinColumn(name = "genre_id")
    private Genre genre;
}
