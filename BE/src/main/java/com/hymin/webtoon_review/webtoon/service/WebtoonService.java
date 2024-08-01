package com.hymin.webtoon_review.webtoon.service;

import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonInfo;
import com.hymin.webtoon_review.webtoon.repository.WebtoonRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebtoonService {

    private final WebtoonRepository webtoonRepository;

    public List<WebtoonInfo> getWentoons(Pageable pageable, String name, List<String> dayOfWeek,
        List<String> platform, List<String> genre) {
        return webtoonRepository.getWebtoons(pageable, name, dayOfWeek, platform, genre);
    }
}
