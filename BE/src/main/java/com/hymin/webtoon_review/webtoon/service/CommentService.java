package com.hymin.webtoon_review.webtoon.service;

import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.webtoon.entity.Comment;
import com.hymin.webtoon_review.webtoon.exception.CommentNotFoundException;
import com.hymin.webtoon_review.webtoon.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public Comment get(Long id) {
        return commentRepository.findById(id)
            .orElseThrow(() -> new CommentNotFoundException(ResponseStatus.COMMENT_NOT_FOUND));
    }

    public void save(Comment comment) {
        commentRepository.save(comment);
    }

    public void delete(Comment comment) {
        commentRepository.delete(comment);
    }
}
