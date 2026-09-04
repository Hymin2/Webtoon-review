package com.hymin.webtoon_review.search.controller;

import com.hymin.webtoon_review.global.response.ApiResponse;
import com.hymin.webtoon_review.global.response.RestResponse;
import com.hymin.webtoon_review.search.service.WebtoonSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class WebtoonSearchController {

    private final WebtoonSearchService webtoonSearchService;

    @GetMapping
    public RestResponse search(
        @RequestParam(name = "query") String query,
        @RequestParam(name = "size", defaultValue = "20", required = false) Integer size,
        @RequestParam(name = "page", defaultValue = "1", required = false) Integer page
    ) {
        return ApiResponse.onSuccess(webtoonSearchService.search(query, size, page));
    }
}
