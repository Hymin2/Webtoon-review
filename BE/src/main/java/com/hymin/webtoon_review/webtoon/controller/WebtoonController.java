package com.hymin.webtoon_review.webtoon.controller;

import com.hymin.webtoon_review.global.annotation.Auth;
import com.hymin.webtoon_review.global.response.ApiResponse;
import com.hymin.webtoon_review.global.response.RestResponse;
import com.hymin.webtoon_review.global.response.SliceResponse;
import com.hymin.webtoon_review.webtoon.dto.WebtoonListResponseDto;
import com.hymin.webtoon_review.webtoon.facade.WebtoonFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
        @PageableDefault(size = 21, sort = "인기순", direction = Direction.DESC) Pageable pageable,
        @RequestParam(name = "value", required = false) String value,
        @RequestParam(name = "dayOfWeek", required = false) String dayOfWeek,
        @RequestParam(name = "genre", required = false) String genre) {
        WebtoonListResponseDto dto = webtoonFacade.getWentoonList(
            pageable,
            value,
            dayOfWeek,
            genre
        );
        return SliceResponse.onSuccess(
            dto.getWebtoonListResponse(),
            pageable.getPageSize(),
            dto.getNext()
        );
    }

    @GetMapping("/{id}")
    public RestResponse getWebtoonDetails(
        @Auth Authentication authentication,
        @PathVariable(name = "id") Long id) {
        return ApiResponse.onSuccess(webtoonFacade.getWebtoonDetails(authentication, id));
    }

    @GetMapping("/categories")
    public RestResponse getWebtoonCategories() {
        return ApiResponse.onSuccess(webtoonFacade.getWebtoonCategories());
    }

    @GetMapping("/hot-webtoons")
    public RestResponse getHotWebtoons() {
        return ApiResponse.onSuccess(webtoonFacade.getHotWebtoonList());
    }

    @PostMapping("/{id}/recommendations")
    public RestResponse addRecommendation(
        @Auth Authentication authentication,
        @PathVariable(name = "id") Long id) {
        webtoonFacade.addRecommend(authentication, id);

        return RestResponse.onCreated();
    }

    @DeleteMapping("/{webtoon_id}/recommendations/{recommendation_id}")
    public RestResponse removeRecommendation(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "recommendation_id") Long recommendationId) {
        webtoonFacade.removeRecommend(authentication, webtoonId, recommendationId);

        return RestResponse.noContent();
    }
}
