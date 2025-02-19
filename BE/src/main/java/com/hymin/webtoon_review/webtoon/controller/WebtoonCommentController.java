package com.hymin.webtoon_review.webtoon.controller;

import com.hymin.webtoon_review.global.annotation.Auth;
import com.hymin.webtoon_review.global.response.RestResponse;
import com.hymin.webtoon_review.webtoon.dto.WebtoonRequest.CommentInfo;
import com.hymin.webtoon_review.webtoon.dto.WebtoonRequest.ReplyInfo;
import com.hymin.webtoon_review.webtoon.facade.WebtoonCommentFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webtoons")
public class WebtoonCommentController {

    private final WebtoonCommentFacade webtoonCommentFacade;

    @PostMapping("/{id}/comments")
    public RestResponse addComment(
        @Auth Authentication authentication,
        @PathVariable(name = "id") Long id,
        @RequestBody CommentInfo commentInfo) {
        webtoonCommentFacade.addComment(authentication, id, commentInfo);

        return RestResponse.onCreated();
    }

    @DeleteMapping("/{webtoon_id}/comments/{comment_id}")
    public RestResponse removeComment(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "comment_id") Long commentId) {
        webtoonCommentFacade.removeComment(authentication, webtoonId, commentId);

        return RestResponse.noContent();
    }

    @PostMapping("/{webtoon_id}/comments/{comment_id}/replies")
    public RestResponse addReply(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "comment_id") Long commentId,
        @RequestBody ReplyInfo replyInfo) {
        webtoonCommentFacade.addReply(authentication, webtoonId, commentId, replyInfo);

        return RestResponse.onCreated();
    }

    @DeleteMapping("/{webtoon_id}/comments/{comment_id}/replies/{reply_id}")
    public RestResponse removeReply(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "comment_id") Long commentId,
        @PathVariable(name = "reply_id") Long replyId) {
        webtoonCommentFacade.removeReply(authentication, webtoonId, commentId, replyId);

        return RestResponse.noContent();
    }

    @PostMapping("/{webtoon_id}/comments/{comment_id}/recommendations")
    public RestResponse addCommentRecommendation(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "comment_id") Long commentId) {
        webtoonCommentFacade.addCommentRecommend(authentication, webtoonId, commentId);

        return RestResponse.noContent();
    }

    @DeleteMapping("/{webtoon_id}/comments/{comment_id}/recommendations/{recommendations_id}")
    public RestResponse removeCommentRecommendation(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "comment_id") Long commentId,
        @PathVariable(name = "recommendations_id") Long recommendId) {
        webtoonCommentFacade.removeCommentRecommend(authentication, webtoonId, commentId,
            recommendId);

        return RestResponse.noContent();
    }

    @PostMapping("/{webtoon_id}/comments/{comment_id}/replies/{reply_id}/recommendations")
    public RestResponse addReplyRecommendation(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "comment_id") Long commentId,
        @PathVariable(name = "reply_id") Long replyId) {
        webtoonCommentFacade.addReplyRecommend(authentication, webtoonId, commentId, replyId);

        return RestResponse.noContent();
    }

    @DeleteMapping("/{webtoon_id}/comments/{comment_id}/replies/{reply_id}/recommendations/{recommend_id}")
    public RestResponse removeReplyRecommendation(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "comment_id") Long commentId,
        @PathVariable(name = "reply_id") Long replyId,
        @PathVariable(name = "recommend_id") Long recommendId) {
        webtoonCommentFacade.removeReplyRecommend(authentication, webtoonId, commentId, replyId,
            recommendId);

        return RestResponse.noContent();
    }
}
