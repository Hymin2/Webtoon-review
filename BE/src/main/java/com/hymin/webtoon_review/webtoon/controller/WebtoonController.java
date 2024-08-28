package com.hymin.webtoon_review.webtoon.controller;

import com.hymin.webtoon_review.global.annotation.Auth;
import com.hymin.webtoon_review.global.response.RestResponse;
import com.hymin.webtoon_review.global.response.SliceResponse;
import com.hymin.webtoon_review.webtoon.service.WebtoonService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webtoons")
public class WebtoonController {

    private final WebtoonService webtoonService;

    @GetMapping
    public RestResponse getWebtoons(
        @Auth Authentication authentication,
        @PageableDefault(size = 10, sort = "updatedAt", direction = Direction.ASC) Pageable pageable,
        @RequestParam(name = "name", required = false) String name,
        @RequestParam(name = "lastValue", required = false) String lastValue,
        @RequestParam(name = "dayOfWeek", required = false) List<String> dayOfWeek,
        @RequestParam(name = "platform", required = false) List<String> platform,
        @RequestParam(name = "genre", required = false) List<String> genre) {
        return SliceResponse.onSuccess(
            webtoonService.getWentoons(authentication, pageable, name, lastValue, dayOfWeek,
                platform, genre),
            pageable.getPageSize());
    }
}
