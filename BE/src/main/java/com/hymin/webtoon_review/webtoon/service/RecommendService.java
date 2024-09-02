package com.hymin.webtoon_review.webtoon.service;

import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.user.entity.WebtoonRecommend;
import com.hymin.webtoon_review.user.exception.RecommendationNotFoundException;
import com.hymin.webtoon_review.webtoon.repository.WebtoonRecommendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final WebtoonRecommendRepository recommendRepository;

    public WebtoonRecommend get(Long id) {
        return recommendRepository.findById(id).orElseThrow(
            () -> new RecommendationNotFoundException(ResponseStatus.RECOMMENDATION_NOT_FOUND));
    }

    public void save(WebtoonRecommend recommend) {
        recommendRepository.save(recommend);
    }

    public void delete(WebtoonRecommend recommend) {
        recommendRepository.delete(recommend);
    }
    
    public void delete(Long id) {
        recommendRepository.deleteById(id);
    }
}
