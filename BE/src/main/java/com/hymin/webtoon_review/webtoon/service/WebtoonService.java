package com.hymin.webtoon_review.webtoon.service;

import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonInfo;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.AuthorSelectResult;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.DayOfWeekSelectResult;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.GenreSelectResult;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.hymin.webtoon_review.webtoon.exception.WebtoonNotFoundException;
import com.hymin.webtoon_review.webtoon.repository.WebtoonRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebtoonService {

    private final WebtoonRepository webtoonRepository;

    @Transactional(readOnly = true)
    public List<WebtoonInfo> getWentoons(Authentication authentication, Pageable pageable,
        String name, List<String> daysOfWeek,
        List<String> platforms, List<String> genres) {

        List<WebtoonInfo> webtoons = webtoonRepository.getWebtoons(
            authentication.getName(),
            pageable,
            name, daysOfWeek,
            platforms, genres);

        List<Long> webtoonIdList = webtoons
            .stream()
            .map(WebtoonInfo::getId)
            .toList();

        List<DayOfWeekSelectResult> dayOfWeekSelectResultList = webtoonRepository.getDayOfWeek(
            webtoonIdList);
        List<GenreSelectResult> genreSelectResultList = webtoonRepository.getGenres(webtoonIdList);
        List<AuthorSelectResult> authorSelectResultList = webtoonRepository.getAuthors(
            webtoonIdList);

        webtoons
            .stream()
            .forEach((webtoon) -> {
                    webtoon.setDayOfWeeks(dayOfWeekSelectResultList
                        .stream()
                        .filter((dow) -> webtoon.getId().equals(dow.getWebtoonId()))
                        .map(DayOfWeekSelectResult::getName)
                        .toList()
                    );

                    webtoon.setGenres(genreSelectResultList
                        .stream()
                        .filter((genre) -> webtoon.getId().equals(genre.getWebtoonId()))
                        .map(GenreSelectResult::getName)
                        .toList());

                    webtoon.setAuthorName(authorSelectResultList
                        .stream()
                        .filter((author) -> webtoon.getId().equals(author.getWebtoonId()))
                        .map(AuthorSelectResult::getName)
                        .toList());
                }
            );

        return webtoons;
    }

    public Webtoon getWebtoon(Long webtoonId) {
        return webtoonRepository.findById(webtoonId).orElseThrow(() -> new WebtoonNotFoundException(
            ResponseStatus.WEBTOON_NOT_FOUND));
    }
}
