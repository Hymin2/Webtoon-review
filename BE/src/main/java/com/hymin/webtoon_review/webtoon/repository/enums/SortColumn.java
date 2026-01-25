package com.hymin.webtoon_review.webtoon.repository.enums;

import com.hymin.webtoon_review.webtoon.entity.QWebtoon;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SortColumn {
    TOTAL_POPULAR("인기순", QWebtoon.webtoon.totalPopularityScore,
        webtoon -> String.valueOf(webtoon.getTotalPopularityScore()),
        value -> value.matches("-?\\d+")),
    MAN_POPULAR("남성 인기순", QWebtoon.webtoon.manPopularityScore,
        webtoon -> String.valueOf(webtoon.getManPopularityScore()),
        value -> value.matches("-?\\d+")),
    FEMALE_POPULAR("여성 인기순", QWebtoon.webtoon.femalePopularityScore,
        webtoon -> String.valueOf(webtoon.getFemalePopularityScore()),
        value -> value.matches("-?\\d+")),
    STAR_SCORE("별점순", QWebtoon.webtoon.totalStarScore,
        webtoon -> String.valueOf(webtoon.getTotalStarScore()),
        value -> value.matches("-?\\d+")),
    RECOMMENDATION("추천순", QWebtoon.webtoon.recommendationCount,
        webtoon -> String.valueOf(webtoon.getRecommendationCount()),
        value -> value.matches("-?\\d+")),
    LATEST("최신순", QWebtoon.webtoon.createdAt,
        webtoon -> String.valueOf(webtoon.getCreatedAt()),
        value -> {
            try {
                LocalDateTime.parse(value);
                return true;
            } catch (DateTimeParseException e) {
                return false;
            }
        });

    private final String property;
    private final ComparableExpressionBase<?> expression;
    private final Function<Webtoon, String> scoreExtractor;
    private final Predicate<String> validator;

    public static SortColumn fromProperty(String property) {
        return Arrays.stream(values())
            .filter(s -> s.property.equals(property))
            .findFirst()
            .orElse(TOTAL_POPULAR);
    }

    public BooleanExpression lt(String value) {
        if (value == null || !validator.test(value)) {
            return null;
        }
        return Expressions.booleanTemplate("{0} < {1}", this.expression, value);
    }

    public BooleanExpression loe(String value) {
        if (value == null || !validator.test(value)) {
            return null;
        }
        return Expressions.booleanTemplate("{0} <= {1}", this.expression, value);
    }

    public String extractScore(Webtoon webtoon) {
        return this.scoreExtractor.apply(webtoon);
    }
}
