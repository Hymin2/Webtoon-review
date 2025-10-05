package com.hymin.webtoon_review.webtoon.facade;

import com.hymin.webtoon_review.global.queue.Job;
import com.hymin.webtoon_review.global.queue.JobQueue;
import com.hymin.webtoon_review.global.queue.TopicNames;
import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.user.entity.Bookmark;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.service.UserService;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.hymin.webtoon_review.webtoon.exception.InvalidBookmarkException;
import com.hymin.webtoon_review.webtoon.mapper.BookmarkMapper;
import com.hymin.webtoon_review.webtoon.mapper.WebtoonMapper;
import com.hymin.webtoon_review.webtoon.service.BookmarkService;
import com.hymin.webtoon_review.webtoon.service.WebtoonService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WebtoonBookmarkFacade {

    private final JobQueue jobQueue;
    private final UserService userService;
    private final WebtoonService webtoonService;
    private final BookmarkService bookmarkService;

    @Transactional
    public void addBookmark(Authentication authentication, Long webtoonId) {
        User user = userService.get(authentication.getName());
        Webtoon webtoon = webtoonService.get(webtoonId);

        bookmarkService.save(BookmarkMapper.toBookmark(user, webtoon));
        jobQueue.add(TopicNames.popularity.name(),
            Job.of(WebtoonMapper.toWebtoonPopularityScore(webtoonId, 2)));
    }

    @Transactional
    public void removeBookmark(Authentication authentication, Long webtoonId, Long bookmarkId) {
        User user = userService.get(authentication.getName());
        Webtoon webtoon = webtoonService.get(webtoonId);
        Bookmark bookmark = bookmarkService.get(bookmarkId);

        if (isInvalidBookmark(user, webtoon, bookmark)) {
            throw new InvalidBookmarkException(ResponseStatus.INVALID_BOOKMARK);
        }

        bookmarkService.delete(bookmark);
        jobQueue.add(TopicNames.popularity.name(),
            Job.of(WebtoonMapper.toWebtoonPopularityScore(webtoonId, -2)));
    }

    private boolean isInvalidBookmark(User user, Webtoon webtoon, Bookmark bookmark) {
        return !bookmark.getUser().equals(user) || !bookmark.getWebtoon().equals(webtoon);
    }
}
