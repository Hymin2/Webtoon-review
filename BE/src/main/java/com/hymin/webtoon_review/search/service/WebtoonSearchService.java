package com.hymin.webtoon_review.search.service;

import com.hymin.webtoon_review.search.dto.WebtoonSearchResponse;
import com.hymin.webtoon_review.search.repository.WebtoonSearchJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebtoonSearchService {

    private final WebtoonSearchJpaRepository webtoonSearchRepository;

    public List<WebtoonSearchResponse> search(String query, Integer page, Integer size) {
        List<Long> ids = webtoonSearchRepository.findWebtoonIds(query, page, size);

        return webtoonSearchRepository.findWebtoonSearchResult(ids).stream().map(
            (w) ->
                WebtoonSearchResponse.builder()
                    .id(w.getId())
                    .name(w.getName())
                    .authors(w.getAuthors())
                    .thumbnail(w.getThumbnail())
                    .build()
        ).toList();
    }
}
