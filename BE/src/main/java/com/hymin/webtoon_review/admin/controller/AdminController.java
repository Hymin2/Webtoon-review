package com.hymin.webtoon_review.admin.controller;

import com.hymin.webtoon_review.admin.service.AdminService;
import com.hymin.webtoon_review.global.response.RestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/data-generator")
    public RestResponse dataGenerator(
        @RequestParam(name = "webtoonSize") Integer webtoonSize,
        @RequestParam(name = "authorSize") Integer authorSize) {
        adminService.dataGenerator(webtoonSize, authorSize);

        return RestResponse.onSuccess();
    }
}
