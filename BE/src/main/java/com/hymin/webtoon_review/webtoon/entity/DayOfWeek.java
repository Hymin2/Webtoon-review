package com.hymin.webtoon_review.webtoon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class DayOfWeek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private com.hymin.webtoon_review.webtoon.entity.enums.DayOfWeek dayOfWeek;

    @OneToMany(mappedBy = "dayOfWeek")
    private List<WebtoonDayOfWeek> webtoonDayOfWeeks;
}
