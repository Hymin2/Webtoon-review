package com.hymin.webtoon_review.webtoon.entity;

import com.hymin.webtoon_review.global.BaseEntity;
import com.hymin.webtoon_review.user.entity.Bookmark;
import com.hymin.webtoon_review.user.entity.WebtoonRecommend;
import com.hymin.webtoon_review.webtoon.entity.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
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
public class Webtoon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "thumbnail", nullable = false)
    private String thumbnail;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private Status status;

    @Column(name = "views")
    private Integer views;

    @Column(name = "recommendation_count")
    private Integer recommendationCount;

    @Column(name = "comment_count")
    private Integer commentCount;

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

    @Column(name = "authors")
    private String authors;

    @Column(name = "day_of_weeks")
    private String dayOfWeeks;

    @Column(name = "genres")
    private String genres;

    @JoinColumn(name = "platform_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Platform platform;

    @OneToMany(mappedBy = "webtoon")
    private List<WebtoonAuthor> webtoonAuthors;

    @OneToMany(mappedBy = "webtoon")
    private List<WebtoonGenre> webtoonGenres;

    @OneToMany(mappedBy = "webtoon")
    private List<WebtoonDayOfWeek> webtoonDayOfWeeks;

    @OneToMany(mappedBy = "webtoon")
    private List<WebtoonRecommend> webtoonRecommends;

    @OneToMany(mappedBy = "webtoon")
    private List<Bookmark> bookmarks;

    @OneToMany(mappedBy = "webtoon")
    private List<Comment> comments;
}
