package com.hymin.webtoon_review.webtoon.controller;

import com.hymin.webtoon_review.global.annotation.Auth;
import com.hymin.webtoon_review.global.response.ApiResponse;
import com.hymin.webtoon_review.global.response.RestResponse;
import com.hymin.webtoon_review.global.response.SliceResponse;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonListResponse;
import com.hymin.webtoon_review.webtoon.facade.WebtoonFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webtoons")
public class WebtoonController {

    private final WebtoonFacade webtoonFacade;

    @GetMapping
    public RestResponse getWebtoons(
            @RequestParam(name = "order", defaultValue = "인기순") String order,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "dayOfWeek", required = false) String dayOfWeek,
            @RequestParam(name = "genre", required = false) String genre,
            @RequestParam(name = "platform", required = false) String platform) {
        WebtoonListResponse dto = webtoonFacade.getWebtoonList(
                order, cursor, dayOfWeek, genre, platform
        );
        return SliceResponse.onSuccess(
                dto.getWebtoonListElements(),
                dto.getWebtoonListElements().size() - 1,
                dto.getNext()
        );
    }

    @GetMapping("/{id}")
    public RestResponse getWebtoonDetails(
            @Auth Authentication authentication,
            @PathVariable(name = "id") Long webtoonId) {
        return ApiResponse.onSuccess(
                webtoonFacade.getWebtoonDetails((Long) authentication.getDetails(), webtoonId));
    }

    @GetMapping("/categories")
    public RestResponse getWebtoonCategories() {
        return ApiResponse.onSuccess(webtoonFacade.getWebtoonCategories());
    }
}
