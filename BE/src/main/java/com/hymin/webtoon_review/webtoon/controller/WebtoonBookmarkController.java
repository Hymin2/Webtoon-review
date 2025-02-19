package com.hymin.webtoon_review.webtoon.controller;

import com.hymin.webtoon_review.global.annotation.Auth;
import com.hymin.webtoon_review.global.response.RestResponse;
import com.hymin.webtoon_review.webtoon.facade.WebtoonBookmarkFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webtoons")
public class WebtoonBookmarkController {

    private final WebtoonBookmarkFacade webtoonBookmarkFacade;

    @PostMapping("/{id}/bookmarks")
    public RestResponse addBookmark(
        @Auth Authentication authentication,
        @PathVariable(name = "id") Long id) {
        webtoonBookmarkFacade.addBookmark(authentication, id);

        return RestResponse.onCreated();
    }

    @DeleteMapping("/{webtoon_id}/bookmarks/{bookmark_id}")
    public RestResponse removeBookmark(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "bookmark_id") Long bookmarkId) {
        webtoonBookmarkFacade.removeBookmark(authentication, webtoonId, bookmarkId);

        return RestResponse.noContent();
    }
}
