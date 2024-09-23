package com.hymin.webtoon_review.webtoon.service;

import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.user.exception.RecommendationNotFoundException;
import com.hymin.webtoon_review.webtoon.entity.CommentRecommend;
import com.hymin.webtoon_review.webtoon.repository.CommentRecommendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentRecommendService {

    private final CommentRecommendRepository commentRecommendRepository;

    public CommentRecommend get(Long id) {
        return commentRecommendRepository.findById(id).orElseThrow(
            () -> new RecommendationNotFoundException(ResponseStatus.RECOMMENDATION_NOT_FOUND));
    }

    public void save(CommentRecommend recommend) {
        commentRecommendRepository.save(recommend);
    }

    public void delete(CommentRecommend recommend) {
        commentRecommendRepository.delete(recommend);
    }

    public void delete(Long id) {
        commentRecommendRepository.deleteById(id);
    }
}
