package com.hymin.webtoon_review.webtoon.service;

import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.user.exception.RecommendationNotFoundException;
import com.hymin.webtoon_review.webtoon.entity.ReplyRecommend;
import com.hymin.webtoon_review.webtoon.repository.ReplyRecommendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReplyRecommendService {

    private final ReplyRecommendRepository replyRecommendRepository;

    public ReplyRecommend get(Long id) {
        return replyRecommendRepository.findById(id).orElseThrow(
            () -> new RecommendationNotFoundException(ResponseStatus.RECOMMENDATION_NOT_FOUND));
    }

    public void save(ReplyRecommend recommend) {
        replyRecommendRepository.save(recommend);
    }

    public void delete(ReplyRecommend recommend) {
        replyRecommendRepository.delete(recommend);
    }

    public void delete(Long id) {
        replyRecommendRepository.deleteById(id);
    }
}
