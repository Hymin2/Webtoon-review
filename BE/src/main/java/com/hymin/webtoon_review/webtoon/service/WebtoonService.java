package com.hymin.webtoon_review.webtoon.service;

import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonInfo;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.Author;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.DayOfWeek;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.Genre;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.Platform;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.hymin.webtoon_review.webtoon.mapper.WebtoonMapper;
import com.hymin.webtoon_review.webtoon.repository.WebtoonRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebtoonService {

    private final WebtoonRepository webtoonRepository;

    public List<WebtoonInfo> getWentoons(Pageable pageable, String name, List<String> daysOfWeek,
        List<String> platforms, List<String> genres) {
        List<Webtoon> webtoons = webtoonRepository.getWebtoons(pageable, name, daysOfWeek,
            platforms, genres);

        List<Long> webtoonIdList = webtoons
            .stream()
            .map(Webtoon::getId)
            .toList();

        List<Platform> platformList = webtoonRepository.getPlatforms(webtoonIdList);
        List<DayOfWeek> dayOfWeekList = webtoonRepository.getDayOfWeek(webtoonIdList);
        List<Genre> genreList = webtoonRepository.getGenres(webtoonIdList);
        List<Author> authorList = webtoonRepository.getAuthors(webtoonIdList);

        return WebtoonMapper.toWebtoonList(webtoons, platformList, dayOfWeekList, genreList,
            authorList);
    }
}
