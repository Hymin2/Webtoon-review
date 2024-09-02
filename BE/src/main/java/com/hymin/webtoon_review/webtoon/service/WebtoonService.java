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

@Service
@RequiredArgsConstructor
public class WebtoonService {

    private final WebtoonRepository webtoonRepository;

    public List<WebtoonInfo> getWebtoonInfoList(Authentication authentication, Pageable pageable,
        String name, String lastValue, List<String> daysOfWeek,
        List<String> platforms, List<String> genres) {
        return webtoonRepository.getWebtoons(
            authentication.getName(),
            pageable,
            name,
            lastValue,
            daysOfWeek,
            platforms, genres);
    }

    public List<DayOfWeekSelectResult> getDayOfWeekSelectResultList(List<Long> webtoonIdList) {
        return webtoonRepository.getDayOfWeek(webtoonIdList);
    }

    public List<GenreSelectResult> getGenreSelectResultList(List<Long> webtoonIdList) {
        return webtoonRepository.getGenres(webtoonIdList);
    }

    public List<AuthorSelectResult> getAuthorSelectResultList(List<Long> webtoonIdList) {
        return webtoonRepository.getAuthors(webtoonIdList);
    }

    public Webtoon get(Long id) {
        return webtoonRepository.findById(id).orElseThrow(() -> new WebtoonNotFoundException(
            ResponseStatus.WEBTOON_NOT_FOUND));
    }
}
