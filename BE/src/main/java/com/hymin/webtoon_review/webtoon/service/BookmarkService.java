package com.hymin.webtoon_review.webtoon.service;

import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.user.entity.Bookmark;
import com.hymin.webtoon_review.user.exception.BookmarkNotFoundException;
import com.hymin.webtoon_review.webtoon.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    public Bookmark get(Long id) {
        return bookmarkRepository.findById(id)
            .orElseThrow(() -> new BookmarkNotFoundException(ResponseStatus.BOOKMARK_NOT_FOUND));
    }

    public void save(Bookmark bookmark) {
        bookmarkRepository.save(bookmark);
    }

    public void delete(Bookmark bookmark) {
        bookmarkRepository.delete(bookmark);
    }

    public void delete(Long id) {
        bookmarkRepository.deleteById(id);
    }
}
