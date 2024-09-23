package com.hymin.webtoon_review.webtoon.service;

import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.webtoon.entity.Reply;
import com.hymin.webtoon_review.webtoon.exception.ReplyNotFoundException;
import com.hymin.webtoon_review.webtoon.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;

    public Reply get(Long id) {
        return replyRepository.findById(id).orElseThrow(() -> new ReplyNotFoundException(
            ResponseStatus.REPLY_NOT_FOUND));
    }

    public Reply save(Reply reply) {
        return replyRepository.save(reply);
    }

    public void delete(Reply reply) {
        replyRepository.delete(reply);
    }
    
    public void delete(Long id) {
        replyRepository.deleteById(id);
    }
}
