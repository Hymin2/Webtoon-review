package com.hymin.webtoon_review.webtoon.dto;

import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonListResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebtoonListResponseDto {

    private List<WebtoonListResponse> webtoonListResponse;
    private String next;
}
