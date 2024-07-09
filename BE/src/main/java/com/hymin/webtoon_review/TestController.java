package com.hymin.webtoon_review;

import com.hymin.webtoon_review.global.response.ApiResponse;
import com.hymin.webtoon_review.global.response.SliceResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("test")
    public ApiResponse<String> test() {
        return ApiResponse.onSuccess("test");
    }

    @GetMapping("slice")
    public ApiResponse<String> sliceTest() {
        return SliceResponse.onSuccess("slice test", false, 1, 0);
    }
}
