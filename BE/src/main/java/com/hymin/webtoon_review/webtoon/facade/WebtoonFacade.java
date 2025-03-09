package com.hymin.webtoon_review.webtoon.facade;

import com.hymin.webtoon_review.global.async.Job;
import com.hymin.webtoon_review.global.async.JobQueue;
import com.hymin.webtoon_review.global.async.TopicNames;
import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.entity.WebtoonRecommend;
import com.hymin.webtoon_review.user.service.UserService;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.Category;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonDetails;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonSimple;
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

    private final JobQueue jobQueue;
    private final UserService userService;
    private final WebtoonService webtoonService;
    private final WebtoonRecommendService webtoonRecommendService;

    @Transactional(readOnly = true)
    public List<WebtoonSimple> getWentoonList(Pageable pageable, String lastValue,
        String daysOfWeek, String genre, String updatedAt) {
        List<WebtoonSimple> webtoonSimpleList = webtoonService.getWebtoonList(
            pageable,
            lastValue,
            daysOfWeek,
            genre,
            updatedAt
        );

        return setWebtoonSimpleList(webtoonSimpleList);
    }

    @Transactional(readOnly = true)
    public List<WebtoonSimple> getHotWebtoonList() {
        return setWebtoonSimpleList(webtoonService.getHotWebtoonList());
    }

    @Transactional(readOnly = true)
    public WebtoonDetails getWebtoonDetails(Authentication authentication, Long id) {
        WebtoonDetails webtoonDetails = webtoonService.get(authentication.getName(), id);
        jobQueue.add(TopicNames.view.name(), Job.of(id));
        jobQueue.add(TopicNames.popularity.name(),
            Job.of(WebtoonMapper.toWebtoonPopularityScore(id, 1)));

        return WebtoonMapper.setWebtoonDetails(
            webtoonDetails,
            webtoonService.getDayOfWeeks(id),
            webtoonService.getGenres(id),
            webtoonService.getAuthors(id),
            domainThumbnail
        );
    }

    @Transactional(readOnly = true)
    public List<Category> getWebtoonCategories() {
        return WebtoonMapper.toWebtoonCategoryList(
            webtoonService.getAllGenres()
        );
    }

    @Transactional
    public void addRecommend(Authentication authentication, Long webtoonId) {
        User user = userService.get(authentication.getName());
        Webtoon webtoon = webtoonService.get(webtoonId);

        webtoonRecommendService.save(WebtoonRecommendMapper.toWebtoonRecommend(user, webtoon));
        webtoonService.increaseRecommendCount(webtoonId);
        jobQueue.add(TopicNames.popularity.name(),
            Job.of(WebtoonMapper.toWebtoonPopularityScore(webtoonId, 1)));
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
        jobQueue.add(TopicNames.popularity.name(),
            Job.of(WebtoonMapper.toWebtoonPopularityScore(webtoonId, -1)));
    }

    private List<WebtoonSimple> setWebtoonSimpleList(List<WebtoonSimple> webtoonSimpleList) {
        List<Long> webtoonIdList = getWebtoonIdList(webtoonSimpleList);

        return WebtoonMapper.setWebtoonSimpleList(
            webtoonSimpleList,
            webtoonService.getDayOfWeekSelectResultList(webtoonIdList),
            webtoonService.getGenreSelectResultList(webtoonIdList),
            webtoonService.getAuthorSelectResultList(webtoonIdList),
            domainThumbnail
        );
    }

    private List<Long> getWebtoonIdList(List<WebtoonSimple> webtoonSimpleList) {
        return webtoonSimpleList.stream()
            .map(WebtoonSimple::getId)
            .toList();
    }

    private boolean isInvalidWebtoonRecommend(User user, Webtoon webtoon,
        WebtoonRecommend webtoonRecommend) {
        return !webtoonRecommend.getUser().equals(user) ||
            !webtoonRecommend.getWebtoon().equals(webtoon);
    }
}
