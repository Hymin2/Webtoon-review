package com.hymin.webtoon_review.webtoon.controller;

import com.hymin.webtoon_review.global.annotation.Auth;
import com.hymin.webtoon_review.global.response.RestResponse;
import com.hymin.webtoon_review.global.response.SliceResponse;
import com.hymin.webtoon_review.webtoon.dto.WebtoonRequest.CommentInfo;
import com.hymin.webtoon_review.webtoon.dto.WebtoonRequest.ReplyInfo;
import com.hymin.webtoon_review.webtoon.facade.WebtoonFacade;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webtoons")
public class WebtoonController {

    private final WebtoonFacade webtoonFacade;

    @GetMapping
    public RestResponse getWebtoons(
        @Auth Authentication authentication,
        @PageableDefault(size = 10, sort = "updatedAt", direction = Direction.ASC) Pageable pageable,
        @RequestParam(name = "name", required = false) String name,
        @RequestParam(name = "lastValue", required = false) String lastValue,
        @RequestParam(name = "dayOfWeek", required = false) List<String> dayOfWeek,
        @RequestParam(name = "platform", required = false) List<String> platform,
        @RequestParam(name = "genre", required = false) List<String> genre) {
        return SliceResponse.onSuccess(
            webtoonFacade.getWentoonList(authentication, pageable, name, lastValue, dayOfWeek,
                platform, genre),
            pageable.getPageSize());
    }

    @GetMapping("/{id}")
    public RestResponse getWebtoon(@PathVariable(name = "id") Long id) {
        webtoonFacade.getWebtoon(id);
        return RestResponse.onSuccess();
    }

    @PostMapping("/{id}/bookmarks")
    public RestResponse addBookmark(
        @Auth Authentication authentication,
        @PathVariable(name = "id") Long id) {
        webtoonFacade.addBookmark(authentication, id);

        return RestResponse.onCreated();
    }

    @DeleteMapping("/{webtoon_id}/bookmarks/{bookmark_id}")
    public RestResponse removeBookmark(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "bookmark_id") Long bookmarkId) {
        webtoonFacade.removeBookmark(authentication, webtoonId, bookmarkId);

        return RestResponse.noContent();
    }

    @PostMapping("/{id}/recommendations")
    public RestResponse addRecommendation(
        @Auth Authentication authentication,
        @PathVariable(name = "id") Long id) {
        webtoonFacade.addRecommendation(authentication, id);

        return RestResponse.onCreated();
    }

    @DeleteMapping("/{webtoon_id}/recommendations/{recommendation_id}")
    public RestResponse removeRecommendation(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "Recommendation_id") Long recommendationId) {
        webtoonFacade.removeRecommendation(authentication, webtoonId, recommendationId);

        return RestResponse.noContent();
    }

    @PostMapping("/{id}/comments")
    public RestResponse addComment(
        @Auth Authentication authentication,
        @PathVariable(name = "id") Long id,
        @RequestBody CommentInfo commentInfo) {
        webtoonFacade.addComment(authentication, id, commentInfo);

        return RestResponse.onCreated();
    }

    @DeleteMapping("/{webtoon_id}/comments/{comment_id}")
    public RestResponse removeComment(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "comment_id") Long commentId) {
        webtoonFacade.removeComment(authentication, webtoonId, commentId);

        return RestResponse.noContent();
    }

    @PostMapping("/{webtoon_id}/comments/{comment_id}/replies")
    public RestResponse addReply(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "comment_id") Long commentId,
        @RequestBody ReplyInfo replyInfo) {
        webtoonFacade.addReply(authentication, webtoonId, commentId, replyInfo);

        return RestResponse.onCreated();
    }

    @DeleteMapping("/{webtoon_id}/comments/{comment_id}/replies/{reply_id}")
    public RestResponse removeReply(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "comment_id") Long commentId,
        @PathVariable(name = "reply_id") Long replyId) {
        webtoonFacade.removeReply(authentication, webtoonId, commentId, replyId);

        return RestResponse.noContent();
    }

    @PostMapping("/{webtoon_id}/comments/{comment_id}/recommendations")
    public RestResponse addCommentRecommendation(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "comment_id") Long commentId) {
        webtoonFacade.addCommentRecommend(authentication, webtoonId, commentId);

        return RestResponse.noContent();
    }

    @DeleteMapping("/{webtoon_id}/comments/{comment_id}/recommendations/{recommendations_id}")
    public RestResponse removeCommentRecommendation(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "comment_id") Long commentId,
        @PathVariable(name = "recommendations_id") Long recommendId) {
        webtoonFacade.removeCommentRecommend(authentication, webtoonId, commentId, recommendId);

        return RestResponse.noContent();
    }

    @PostMapping("/{webtoon_id}/comments/{comment_id}/replies/{reply_id}/recommendations")
    public RestResponse addReplyRecommendation(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "comment_id") Long commentId,
        @PathVariable(name = "reply_id") Long replyId) {
        webtoonFacade.addReplyRecommend(authentication, webtoonId, commentId, replyId);

        return RestResponse.noContent();
    }

    @DeleteMapping("/{webtoon_id}/comments/{comment_id}/replies/{reply_id}/recommendations/{recommend_id}")
    public RestResponse removeReplyRecommendation(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "comment_id") Long commentId,
        @PathVariable(name = "reply_id") Long replyId,
        @PathVariable(name = "recommend_id") Long recommendId) {
        webtoonFacade.removeReplyRecommend(authentication, webtoonId, commentId, replyId,
            recommendId);

        return RestResponse.noContent();
    }
}
