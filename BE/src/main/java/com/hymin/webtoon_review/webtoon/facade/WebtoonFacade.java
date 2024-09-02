package com.hymin.webtoon_review.webtoon.facade;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.user.entity.Bookmark;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.entity.WebtoonRecommend;
import com.hymin.webtoon_review.user.service.UserService;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonInfo;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.AuthorSelectResult;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.DayOfWeekSelectResult;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.GenreSelectResult;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.hymin.webtoon_review.webtoon.mapper.BookmarkMapper;
import com.hymin.webtoon_review.webtoon.mapper.RecommendMapper;
import com.hymin.webtoon_review.webtoon.service.BookmarkService;
import com.hymin.webtoon_review.webtoon.service.RecommendService;
import com.hymin.webtoon_review.webtoon.service.WebtoonService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WebtoonFacade {

    private final UserService userService;
    private final WebtoonService webtoonService;
    private final BookmarkService bookmarkService;
    private final RecommendService recommendService;

    @Transactional(readOnly = true)
    public List<WebtoonInfo> getWentoonList(Authentication authentication, Pageable pageable,
        String name, String lastValue, List<String> daysOfWeek,
        List<String> platforms, List<String> genres
    ) {
        List<WebtoonInfo> webtoonInfoList = webtoonService.getWebtoonInfoList(authentication,
            pageable, name, lastValue, daysOfWeek, platforms, genres);
        List<Long> webtoonIdList = getWebtoonIdList(webtoonInfoList);
        List<DayOfWeekSelectResult> dayOfWeekSelectResultList = webtoonService.getDayOfWeekSelectResultList(
            webtoonIdList);
        List<GenreSelectResult> genreSelectResultList = webtoonService.getGenreSelectResultList(
            webtoonIdList);
        List<AuthorSelectResult> authorSelectResultList = webtoonService.getAuthorSelectResultList(
            webtoonIdList);

        webtoonInfoList
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

        return webtoonInfoList;
    }

    @Transactional
    public void addBookmark(Authentication authentication, Long webtoonId) {
        User user = userService.get(authentication.getName());
        Webtoon webtoon = webtoonService.get(webtoonId);

        bookmarkService.save(BookmarkMapper.toBookmark(user, webtoon));
    }

    @Transactional
    public void removeBookmark(Authentication authentication, Long webtoonId, Long bookmarkId) {
        User user = userService.get(authentication.getName());
        Bookmark bookmark = bookmarkService.get(bookmarkId);

        if (!bookmark.getUser().equals(user) ||
            !bookmark.getWebtoon().getId().equals(webtoonId)) {
            throw new GeneralException(ResponseStatus.BAD_REQUEST);
        }

        bookmarkService.delete(bookmark);
    }

    @Transactional
    public void addRecommendation(Authentication authentication, Long webtoonId) {
        User user = userService.get(authentication.getName());
        Webtoon webtoon = webtoonService.get(webtoonId);

        recommendService.save(RecommendMapper.toWebtoonRecommend(user, webtoon));
    }

    @Transactional
    public void removeRecommendation(Authentication authentication, Long webtoonId,
        Long recommendId) {
        User user = userService.get(authentication.getName());
        WebtoonRecommend webtoonRecommend = recommendService.get(recommendId);

        if (!webtoonRecommend.getUser().equals(user) ||
            !webtoonRecommend.getWebtoon().getId().equals(webtoonId)) {
            throw new GeneralException(ResponseStatus.BAD_REQUEST);
        }

        recommendService.delete(webtoonRecommend);
    }

    private List<Long> getWebtoonIdList(List<WebtoonInfo> webtoonInfoList) {
        return webtoonInfoList.stream()
            .map(WebtoonInfo::getId)
            .toList();
    }
}
