package com.hymin.webtoon_review.webtoon.facade;

import com.hymin.webtoon_review.global.async.Job;
import com.hymin.webtoon_review.global.async.JobQueue;
import com.hymin.webtoon_review.global.async.TopicNames;
import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.service.UserService;
import com.hymin.webtoon_review.webtoon.dto.WebtoonRequest.CommentInfo;
import com.hymin.webtoon_review.webtoon.dto.WebtoonRequest.ReplyInfo;
import com.hymin.webtoon_review.webtoon.entity.Comment;
import com.hymin.webtoon_review.webtoon.entity.CommentRecommend;
import com.hymin.webtoon_review.webtoon.entity.Reply;
import com.hymin.webtoon_review.webtoon.entity.ReplyRecommend;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.hymin.webtoon_review.webtoon.exception.InvalidCommentException;
import com.hymin.webtoon_review.webtoon.exception.InvalidCommentRecommendException;
import com.hymin.webtoon_review.webtoon.exception.InvalidReplyException;
import com.hymin.webtoon_review.webtoon.exception.InvalidReplyRecommendException;
import com.hymin.webtoon_review.webtoon.mapper.CommentMapper;
import com.hymin.webtoon_review.webtoon.mapper.CommentRecommendMapper;
import com.hymin.webtoon_review.webtoon.mapper.ReplyMapper;
import com.hymin.webtoon_review.webtoon.mapper.ReplyRecommendMapper;
import com.hymin.webtoon_review.webtoon.mapper.WebtoonMapper;
import com.hymin.webtoon_review.webtoon.service.CommentRecommendService;
import com.hymin.webtoon_review.webtoon.service.CommentService;
import com.hymin.webtoon_review.webtoon.service.ReplyRecommendService;
import com.hymin.webtoon_review.webtoon.service.ReplyService;
import com.hymin.webtoon_review.webtoon.service.WebtoonService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WebtoonCommentFacade {

    private final JobQueue jobQueue;
    private final UserService userService;
    private final ReplyService replyService;
    private final WebtoonService webtoonService;
    private final CommentService commentService;
    private final ReplyRecommendService replyRecommendService;
    private final CommentRecommendService commentRecommendService;

    @Transactional
    public void addComment(Authentication authentication, Long id, CommentInfo commentInfo) {
        User user = userService.get(authentication.getName());
        Webtoon webtoon = webtoonService.get(id);

        commentService.save(CommentMapper.toComment(user, webtoon, commentInfo));
        webtoonService.increaseCommentCount(id);
        webtoonService.increaseStarScore(id, commentInfo.getScore());
        jobQueue.add(TopicNames.popularity.name(),
            Job.of(
                WebtoonMapper.toWebtoonPopularityScore(id, commentInfo.getScore().intValue() / 3)));
    }

    @Transactional
    public void removeComment(Authentication authentication, Long webtoonId, Long commentId) {
        User user = userService.get(authentication.getName());
        Webtoon webtoon = webtoonService.get(webtoonId);
        Comment comment = commentService.get(commentId);

        if (isInvalidComment(user, webtoon, comment)) {
            throw new InvalidCommentException(ResponseStatus.INVALID_COMMENT);
        }

        webtoonService.decreaseCommentCount(webtoonId);
        int score = comment.getScore().intValue() / 3;
        comment.delete();
        jobQueue.add(TopicNames.popularity.name(),
            Job.of(WebtoonMapper.toWebtoonPopularityScore(webtoonId, score)));
    }

    @Transactional
    public void addReply(Authentication authentication, Long webtoonId, Long commentId,
        ReplyInfo replyInfo) {
        User user = userService.get(authentication.getName());
        Webtoon webtoon = webtoonService.get(webtoonId);
        Comment comment = commentService.get(commentId);

        if (isInvalidComment(user, webtoon, comment)) {
            throw new InvalidCommentException(ResponseStatus.INVALID_COMMENT);
        }

        replyService.save(ReplyMapper.toReply(user, comment, replyInfo));
    }

    @Transactional
    public void removeReply(Authentication authentication, Long webtoonId, Long commentId,
        Long replyId) {
        User user = userService.get(authentication.getName());
        Webtoon webtoon = webtoonService.get(webtoonId);
        Comment comment = commentService.get(commentId);
        Reply reply = replyService.get(replyId);

        if (isInvalidReply(user, webtoon, comment, reply)) {
            throw new InvalidReplyException(ResponseStatus.INVALID_REPLY);
        }

        reply.delete();
    }

    @Transactional
    public void addCommentRecommend(Authentication authentication, Long webtoonId, Long commentId) {
        User user = userService.get(authentication.getName());
        Webtoon webtoon = webtoonService.get(webtoonId);
        Comment comment = commentService.get(commentId);

        if (isInvalidComment(webtoon, comment)) {
            throw new InvalidCommentException(ResponseStatus.INVALID_COMMENT);
        }

        commentRecommendService.save(CommentRecommendMapper.toCommentRecommend(user, comment));
    }

    @Transactional
    public void removeCommentRecommend(Authentication authentication, Long webtoonId,
        Long commentId, Long recommendId) {
        User user = userService.get(authentication.getName());
        Webtoon webtoon = webtoonService.get(webtoonId);
        Comment comment = commentService.get(commentId);
        CommentRecommend commentRecommend = commentRecommendService.get(recommendId);

        if (isInvalidCommentRecommend(user, webtoon, comment, commentRecommend)) {
            throw new InvalidCommentRecommendException(ResponseStatus.INVALID_COMMENT_RECOMMEND);
        }

        commentRecommendService.delete(commentRecommend);
    }

    @Transactional
    public void addReplyRecommend(Authentication authentication, Long webtoonId, Long commentId,
        Long replyId) {
        User user = userService.get(authentication.getName());
        Webtoon webtoon = webtoonService.get(webtoonId);
        Comment comment = commentService.get(commentId);
        Reply reply = replyService.get(replyId);

        if (isInvalidReply(webtoon, comment, reply)) {
            throw new InvalidReplyException(ResponseStatus.INVALID_REPLY);
        }

        replyRecommendService.save(ReplyRecommendMapper.toReplyRecommend(user, reply));
    }

    @Transactional
    public void removeReplyRecommend(Authentication authentication, Long webtoonId, Long commentId,
        Long replyId, Long recommendId) {
        User user = userService.get(authentication.getName());
        Webtoon webtoon = webtoonService.get(webtoonId);
        Comment comment = commentService.get(commentId);
        Reply reply = replyService.get(replyId);
        ReplyRecommend replyRecommend = replyRecommendService.get(recommendId);

        if (isInvalidReplyRecommend(user, webtoon, comment, reply, replyRecommend)) {
            throw new InvalidReplyRecommendException(ResponseStatus.INVALID_REPLY_RECOMMEND);
        }

        replyRecommendService.delete(replyRecommend);
    }

    private boolean isInvalidComment(Webtoon webtoon, Comment comment) {
        return !comment.getWebtoon().equals(webtoon);
    }

    private boolean isInvalidComment(User user, Webtoon webtoon, Comment comment) {
        return isInvalidComment(webtoon, comment) || !comment.getUser().equals(user);
    }

    private boolean isInvalidReply(Webtoon webtoon, Comment comment, Reply reply) {
        return isInvalidComment(webtoon, comment) || !reply.getComment().equals(comment);
    }

    private boolean isInvalidReply(User user, Webtoon webtoon, Comment comment, Reply reply) {
        return isInvalidReply(webtoon, comment, reply) ||
            !reply.getUser().equals(user);
    }

    private boolean isInvalidCommentRecommend(Webtoon webtoon, Comment comment,
        CommentRecommend commentRecommend) {
        return isInvalidComment(webtoon, comment) || !commentRecommend.getComment().equals(comment);
    }

    private boolean isInvalidCommentRecommend(User user, Webtoon webtoon, Comment comment,
        CommentRecommend commentRecommend) {
        return isInvalidCommentRecommend(webtoon, comment, commentRecommend)
            || !commentRecommend.getUser().equals(user);
    }

    private boolean isInvalidReplyRecommend(Webtoon webtoon, Comment comment, Reply reply,
        ReplyRecommend replyRecommend) {
        return isInvalidReply(webtoon, comment, reply) ||
            !replyRecommend.getReply().equals(reply);
    }

    private boolean isInvalidReplyRecommend(User user, Webtoon webtoon, Comment comment,
        Reply reply, ReplyRecommend replyRecommend) {
        return isInvalidReplyRecommend(webtoon, comment, reply, replyRecommend) ||
            !replyRecommend.getUser().equals(user);
    }
}
