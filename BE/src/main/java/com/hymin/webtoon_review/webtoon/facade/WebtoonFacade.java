package com.hymin.webtoon_review.webtoon.facade;

import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.entity.WebtoonRecommend;
import com.hymin.webtoon_review.user.service.UserService;
import com.hymin.webtoon_review.webtoon.dto.WebtoonListResponseDto;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.Category;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.HotWebtoonListResponse;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonDetails;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.hymin.webtoon_review.webtoon.exception.InvalidWebtoonRecommendException;
import com.hymin.webtoon_review.webtoon.mapper.WebtoonMapper;
import com.hymin.webtoon_review.webtoon.mapper.WebtoonRecommendMapper;
import com.hymin.webtoon_review.webtoon.service.WebtoonRecommendService;
import com.hymin.webtoon_review.webtoon.service.WebtoonService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebtoonFacade {

    @Value("${domain.thumbnail}")
    private String domainThumbnail;

    private final UserService userService;
    private final WebtoonService webtoonService;
    private final WebtoonRecommendService webtoonRecommendService;

    @Transactional(readOnly = true)
    public WebtoonListResponseDto getWentoonList(Pageable pageable, String lastValue,
        String daysOfWeek, String genre) {
        return WebtoonMapper.toWebtoonListResponseDto(
            webtoonService.getWebtoonList(
                pageable,
                lastValue,
                webtoonService.getDayOfWeek(daysOfWeek),
                webtoonService.getGenre(genre)
            ), domainThumbnail, pageable);
    }

    @Transactional(readOnly = true)
    public List<HotWebtoonListResponse> getHotWebtoonList() {
        return WebtoonMapper.toHotWebtoonListResponse(webtoonService.getHotWebtoonList(),
            domainThumbnail);
    }

    @Transactional(readOnly = true)
    public WebtoonDetails getWebtoonDetails(Authentication authentication, Long id) {
        return webtoonService.get(authentication.getName(), id);
    }

    @Transactional(readOnly = true)
    public List<Category> getWebtoonCategories() {
        return WebtoonMapper.toWebtoonCategoryList(
            webtoonService.getAllGenre()
        );
    }

    @Transactional
    public void addRecommend(Authentication authentication, Long webtoonId) {
        User user = userService.get(authentication.getName());
        Webtoon webtoon = webtoonService.get(webtoonId);

        webtoonRecommendService.save(WebtoonRecommendMapper.toWebtoonRecommend(user, webtoon));
        webtoonService.increaseRecommendCount(webtoonId);
    }

    @Transactional
    public void removeRecommend(Authentication authentication, Long webtoonId, Long recommendId) {
        User user = userService.get(authentication.getName());
        Webtoon webtoon = webtoonService.get(webtoonId);
        WebtoonRecommend webtoonRecommend = webtoonRecommendService.get(recommendId);

        if (isInvalidWebtoonRecommend(user, webtoon, webtoonRecommend)) {
            throw new InvalidWebtoonRecommendException(ResponseStatus.INVALID_WEBTOON_RECOMMEND);
        }

        webtoonRecommendService.delete(webtoonRecommend);
        webtoonService.decreaseRecommendCount(webtoonId);
    }

    private boolean isInvalidWebtoonRecommend(User user, Webtoon webtoon,
        WebtoonRecommend webtoonRecommend) {
        return !webtoonRecommend.getUser().equals(user) ||
            !webtoonRecommend.getWebtoon().equals(webtoon);
    }
}
