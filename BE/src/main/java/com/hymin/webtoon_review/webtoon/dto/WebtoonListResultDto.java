package com.hymin.webtoon_review.webtoon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebtoonListResultDto {

    private Long id;
    private String name;
    private String thumbnail;
    private String authorName;
    private String dayOfWeek;
    private String genre;
    @Setter
    private Integer starRating;
    @Setter
    private Integer popularityScore;

    public WebtoonListResultDto(
        Long id, String name, String thumbnail, String authorName, String dayOfWeek, String genre
    ) {
        this.id = id;
        this.name = name;
        this.thumbnail = thumbnail;
        this.authorName = authorName;
        this.dayOfWeek = dayOfWeek;
        this.genre = genre;
    }
}
