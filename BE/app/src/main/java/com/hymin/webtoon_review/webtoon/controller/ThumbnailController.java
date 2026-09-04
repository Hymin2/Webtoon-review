package com.hymin.webtoon_review.webtoon.controller;

import com.hymin.webtoon_review.webtoon.facade.ThumbnailFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webtoons/thumbnails")
public class ThumbnailController {

    private final ThumbnailFacade thumbnailFacade;

    @GetMapping(value = "/{name}", produces = {MediaType.IMAGE_GIF_VALUE,
        MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> getThumbnail(@PathVariable(name = "name") String name) {
        return ResponseEntity.ok(thumbnailFacade.getThumbnail(name));
    }
}
