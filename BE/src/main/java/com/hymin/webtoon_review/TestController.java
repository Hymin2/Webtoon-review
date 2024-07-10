package com.hymin.webtoon_review;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ApiResponse;
import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.global.response.RestResponse;
import com.hymin.webtoon_review.global.response.SliceResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping
    public RestResponse test() {
        return ApiResponse.onSuccess("test");
    }

    @GetMapping("/slice")
    public RestResponse sliceTest() {
        return SliceResponse.onSuccess("slice test", false, 1, 0);
    }

    @GetMapping("/error")
    public RestResponse errorTest() {
        throw new GeneralException(ResponseStatus.BAD_REQUEST);
    }
}
