package com.hymin.webtoon_review.webtoon.facade;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hymin.webtoon_review.global.async.Job;
import com.hymin.webtoon_review.global.async.JobQueue;
import com.hymin.webtoon_review.global.async.TopicNames;
import com.hymin.webtoon_review.user.entity.Bookmark;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.service.UserService;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.hymin.webtoon_review.webtoon.exception.InvalidBookmarkException;
import com.hymin.webtoon_review.webtoon.service.BookmarkService;
import com.hymin.webtoon_review.webtoon.service.WebtoonService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class WebtoonBookmarkFacadeTest {

    @InjectMocks
    private WebtoonBookmarkFacade webtoonBookmarkFacade;
    @Mock
    private JobQueue jobQueue;
    @Mock
    private UserService userService;
    @Mock
    private WebtoonService webtoonService;
    @Mock
    private BookmarkService bookmarkService;
    @Mock
    private Authentication authentication;

    private static User user;
    private static Webtoon webtoon;
    private static Bookmark bookmark;

    @BeforeAll
    public static void init() {
        user = User.builder()
            .username("user")
            .build();
        webtoon = Webtoon.builder()
            .id(1L)
            .build();
        bookmark = Bookmark.builder()
            .user(user)
            .webtoon(webtoon)
            .build();
    }

    @Test
    @DisplayName("북마크 등록 성공 테스트")
    public void testAddBookmark() {
        // Given
        Long webtoonId = 1L;

        when(authentication.getName()).thenReturn("user");
        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);

        // When
        webtoonBookmarkFacade.addBookmark(authentication, webtoonId);

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(bookmarkService, times(1)).save(any(Bookmark.class));
        verify(jobQueue, times(1)).add(eq(TopicNames.popularity.name()), any(Job.class));
    }

    @Test
    @DisplayName("북마크 제거 성공 테스트")
    public void testRemoveBookmark() {
        // Given
        Long webtoonId = 1L;
        Long bookmarkId = 2L;

        when(authentication.getName()).thenReturn("user");
        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(bookmarkService.get(bookmarkId)).thenReturn(bookmark);

        // When
        webtoonBookmarkFacade.removeBookmark(authentication, webtoonId, bookmarkId);

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(bookmarkService, times(1)).get(bookmarkId);
        verify(bookmarkService, times(1)).delete(bookmark);
        verify(jobQueue, times(1)).add(eq(TopicNames.popularity.name()), any(Job.class));
    }

    @Test
    @DisplayName("잘못된 사용자로 북마크 삭제 요청 시 예외 발생 테스트")
    public void testRemoveBookmarkWithInvalidUser() {
        // Given
        User originUser = new User();
        Bookmark bookmark = Bookmark.builder()
            .user(originUser)
            .webtoon(webtoon)
            .build();
        Long webtoonId = 1L;
        Long bookmarkId = 2L;

        when(authentication.getName()).thenReturn("user");
        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(bookmarkService.get(bookmarkId)).thenReturn(bookmark);

        // When
        assertThrows(InvalidBookmarkException.class, () ->
            webtoonBookmarkFacade.removeBookmark(authentication, webtoonId, bookmarkId)
        );

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(bookmarkService, times(1)).get(bookmarkId);
        verify(bookmarkService, never()).delete(bookmark);
        verify(jobQueue, never()).add(eq(TopicNames.popularity.name()), any(Job.class));
    }

    @Test
    @DisplayName("잘못된 웹툰ID로 북마크 삭제 요청 시 예외 발생 테스트")
    public void testRemoveBookmarkWithInvalidWebtoon() {
        // Given
        Webtoon originWebtoon = new Webtoon();
        Bookmark bookmark = Bookmark.builder()
            .user(user)
            .webtoon(originWebtoon)
            .build();
        Long webtoonId = 1L;
        Long bookmarkId = 2L;

        when(authentication.getName()).thenReturn("user");
        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(bookmarkService.get(bookmarkId)).thenReturn(bookmark);

        // When
        assertThrows(InvalidBookmarkException.class, () ->
            webtoonBookmarkFacade.removeBookmark(authentication, webtoonId, bookmarkId)
        );

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(bookmarkService, times(1)).get(bookmarkId);
        verify(bookmarkService, never()).delete(bookmark);
        verify(jobQueue, never()).add(eq(TopicNames.popularity.name()), any(Job.class));
    }
}