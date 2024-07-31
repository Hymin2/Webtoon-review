package com.hymin.webtoon_review.webtoon.entity;

import com.hymin.webtoon_review.global.BaseEntity;
import com.hymin.webtoon_review.webtoon.entity.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @ManyToOne
    @JoinColumn(name = "platform_id")
    private Platform platform;

    @OneToMany(mappedBy = "webtoon")
    private List<WebtoonAuthor> webtoonAuthors;

    @OneToMany(mappedBy = "webtoon")
    private List<WebtoonGenre> webtoonGenres;

    @OneToMany(mappedBy = "webtoon")
    private List<WebtoonDayOfWeek> webtoonDayOfWeeks;
}
