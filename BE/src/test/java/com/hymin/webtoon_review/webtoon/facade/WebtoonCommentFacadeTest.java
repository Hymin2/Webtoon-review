package com.hymin.webtoon_review.webtoon.facade;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hymin.webtoon_review.global.async.Job;
import com.hymin.webtoon_review.global.async.JobQueue;
import com.hymin.webtoon_review.global.async.TopicNames;
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
import com.hymin.webtoon_review.webtoon.service.CommentRecommendService;
import com.hymin.webtoon_review.webtoon.service.CommentService;
import com.hymin.webtoon_review.webtoon.service.ReplyRecommendService;
import com.hymin.webtoon_review.webtoon.service.ReplyService;
import com.hymin.webtoon_review.webtoon.service.WebtoonService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@Nested
@DisplayName("웹툰 댓글 관련 테스트")
@ExtendWith(MockitoExtension.class)
class WebtoonCommentFacadeTest {

    @Mock
    private JobQueue jobQueue;
    @Mock
    private UserService userService;
    @Mock
    private ReplyService replyService;
    @Mock
    private WebtoonService webtoonService;
    @Mock
    private CommentService commentService;
    @Mock
    private ReplyRecommendService replyRecommendService;
    @Mock
    private CommentRecommendService commentRecommendService;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private WebtoonCommentFacade webtoonCommentFacade;

    @Test
    @DisplayName("댓글 등록 성공 테스트")
    public void testAddComment() {
        // Given
        User user = new User();
        Webtoon webtoon = new Webtoon();
        CommentInfo commentInfo = CommentInfo.builder()
            .score(5.0)
            .content("")
            .build();
        Long webtoonId = 1L;

        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(authentication.getName()).thenReturn("user");

        // When
        webtoonCommentFacade.addComment(authentication, webtoonId, commentInfo);

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).save(any(Comment.class));
        verify(webtoonService, times(1)).increaseCommentCount(webtoonId);
        verify(webtoonService, times(1)).increaseStarScore(webtoonId, commentInfo.getScore());
        verify(jobQueue, times(1)).add(eq(TopicNames.popularity.name()), any(Job.class));
    }

    @Test
    @DisplayName("댓글 삭제 성공 테스트")
    public void testRemoveComment() {
        // Given
        User user = new User();
        Webtoon webtoon = new Webtoon();
        Comment comment = Mockito.spy(Comment.builder()
            .user(user)
            .webtoon(webtoon)
            .score(5.0)
            .build());
        Long webtoonId = 1L;
        Long commentId = 2L;

        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(commentService.get(commentId)).thenReturn(comment);
        when(authentication.getName()).thenReturn("user");

        // When
        webtoonCommentFacade.removeComment(authentication, webtoonId, commentId);

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).get(commentId);
        verify(webtoonService, times(1)).decreaseCommentCount(webtoonId);
        verify(comment, times(1)).delete();
        verify(jobQueue, times(1)).add(eq(TopicNames.popularity.name()), any(Job.class));
    }

    @Test
    @DisplayName("잘못된 웹툰ID로 댓글 삭제 요청 시 예외 발생 테스트")
    public void testRemoveCommentWithInvalidWebtoon() {
        // Given
        User user = new User();
        Webtoon webtoon = new Webtoon();
        Webtoon originWebtoon = new Webtoon();
        Comment comment = Mockito.spy(Comment.builder()
            .user(user)
            .webtoon(originWebtoon)
            .score(5.0)
            .build());
        Long webtoonId = 1L;
        Long commentId = 2L;

        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(commentService.get(commentId)).thenReturn(comment);
        when(authentication.getName()).thenReturn("user");

        // When
        assertThrows(InvalidCommentException.class,
            () -> webtoonCommentFacade.removeComment(authentication, webtoonId, commentId));

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).get(commentId);
        verify(webtoonService, never()).decreaseCommentCount(webtoonId);
        verify(comment, never()).delete();
        verify(jobQueue, never()).add(eq(TopicNames.popularity.name()), any(Job.class));
    }

    @Test
    @DisplayName("잘못된 사용자로 댓글 삭제 요청 시 예외 발생 테스트")
    public void testRemoveCommentWithInvalidUser() {
        // Given
        User user = new User();
        User originUser = new User();
        Webtoon webtoon = new Webtoon();
        Comment comment = Mockito.spy(Comment.builder()
            .user(originUser)
            .webtoon(webtoon)
            .score(5.0)
            .build());
        Long webtoonId = 1L;
        Long commentId = 2L;

        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(commentService.get(commentId)).thenReturn(comment);
        when(authentication.getName()).thenReturn("user");

        // When
        assertThrows(InvalidCommentException.class,
            () -> webtoonCommentFacade.removeComment(authentication, webtoonId, commentId));

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).get(commentId);
        verify(webtoonService, never()).decreaseCommentCount(webtoonId);
        verify(comment, never()).delete();
        verify(jobQueue, never()).add(eq(TopicNames.popularity.name()), any(Job.class));
    }

    @Test
    @DisplayName("답글 작성 성공 테스트")
    public void testAddReply() {
        // Given
        User user = new User();
        Webtoon webtoon = new Webtoon();
        Comment comment = Comment.builder()
            .user(user)
            .webtoon(webtoon)
            .build();
        Long webtoonId = 1L;
        Long commentId = 2L;
        ReplyInfo replyInfo = new ReplyInfo();

        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(commentService.get(commentId)).thenReturn(comment);
        when(authentication.getName()).thenReturn("user");

        // When
        webtoonCommentFacade.addReply(authentication, webtoonId, commentId, replyInfo);

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).get(commentId);
        verify(replyService, times(1)).save(any(Reply.class));
    }

    @Test
    @DisplayName("댓글이 작성된 웹툰과 웹툰ID가 다를 때 답글 작성 요청 시 예외 발생 테스트")
    public void testAddReplyWhenWebtoonMismatch() {
        // Given
        User user = new User();
        Webtoon webtoon = new Webtoon();
        Webtoon originWebtoon = new Webtoon();
        Comment comment = Comment.builder()
            .user(user)
            .webtoon(originWebtoon)
            .build();
        Long webtoonId = 1L;
        Long commentId = 2L;
        ReplyInfo replyInfo = new ReplyInfo();

        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(commentService.get(commentId)).thenReturn(comment);
        when(authentication.getName()).thenReturn("user");

        // When
        assertThrows(InvalidCommentException.class,
            () -> webtoonCommentFacade.addReply(authentication, webtoonId, commentId, replyInfo));

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).get(commentId);
        verify(replyService, never()).save(any(Reply.class));
    }

    @Test
    @DisplayName("답글 삭제 성공 테스트")
    public void testRemoveReply() {
        // Given
        User commentUser = new User();
        User replyUser = new User();
        Webtoon webtoon = new Webtoon();
        Comment comment = Comment.builder()
            .user(commentUser)
            .webtoon(webtoon)
            .build();
        Reply reply = Mockito.spy(Reply.builder()
            .user(replyUser)
            .comment(comment)
            .build());
        Long webtoonId = 1L;
        Long commentId = 2L;
        Long replyId = 3L;

        when(userService.get("user")).thenReturn(replyUser);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(commentService.get(commentId)).thenReturn(comment);
        when(replyService.get(replyId)).thenReturn(reply);
        when(authentication.getName()).thenReturn("user");

        // When
        webtoonCommentFacade.removeReply(authentication, webtoonId, commentId, replyId);

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).get(commentId);
        verify(replyService, times(1)).get(replyId);
        verify(reply, times(1)).delete();
    }

    @Test
    @DisplayName("잘못된 사용자로 답글 삭제 요청 시 예외 발생 테스트")
    public void testRemoveReplyWhenUserMismatch() {
        // Given
        User commentUser = new User();
        User user = new User();
        User originReplyUser = new User();
        Webtoon webtoon = new Webtoon();
        Comment comment = Comment.builder()
            .user(commentUser)
            .webtoon(webtoon)
            .build();
        Reply reply = Mockito.spy(Reply.builder()
            .user(originReplyUser)
            .comment(comment)
            .build());
        Long webtoonId = 1L;
        Long commentId = 2L;
        Long replyId = 3L;

        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(commentService.get(commentId)).thenReturn(comment);
        when(replyService.get(replyId)).thenReturn(reply);
        when(authentication.getName()).thenReturn("user");

        // When
        assertThrows(InvalidReplyException.class,
            () -> webtoonCommentFacade.removeReply(authentication, webtoonId, commentId, replyId));

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).get(commentId);
        verify(replyService, times(1)).get(replyId);
        verify(reply, never()).delete();
    }

    @Test
    @DisplayName("답글의 댓글과 댓글ID가 다를 때 답글 삭제 요청 시 예외 발생 테스트")
    public void testRemoveReplyWhenCommentMismatch() {
        // Given
        User user = new User();
        User commentUser = new User();
        Webtoon webtoon = new Webtoon();
        Comment comment = Comment.builder()
            .user(commentUser)
            .webtoon(webtoon)
            .build();
        Comment originComment = Comment.builder()
            .user(commentUser)
            .webtoon(webtoon)
            .build();
        Reply reply = Mockito.spy(Reply.builder()
            .user(user)
            .comment(originComment)
            .build());
        Long webtoonId = 1L;
        Long commentId = 2L;
        Long replyId = 3L;

        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(commentService.get(commentId)).thenReturn(comment);
        when(replyService.get(replyId)).thenReturn(reply);
        when(authentication.getName()).thenReturn("user");

        // When
        assertThrows(InvalidReplyException.class,
            () -> webtoonCommentFacade.removeReply(authentication, webtoonId, commentId, replyId));

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).get(commentId);
        verify(replyService, times(1)).get(replyId);
        verify(reply, never()).delete();
    }

    @Test
    @DisplayName("웹툰ID와 댓글이 달린 웹툰이 다를 때 답글 삭제 요청 시 예외 발생 테스트")
    public void testRemoveReplyWhenWebtoonMismatch() {
        // Given
        User user = new User();
        User commentUser = new User();
        Webtoon webtoon = new Webtoon();
        Webtoon originWebtoon = new Webtoon();
        Comment comment = Comment.builder()
            .user(commentUser)
            .webtoon(originWebtoon)
            .build();
        Reply reply = Mockito.spy(Reply.builder()
            .user(user)
            .comment(comment)
            .build());
        Long webtoonId = 1L;
        Long commentId = 2L;
        Long replyId = 3L;

        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(commentService.get(commentId)).thenReturn(comment);
        when(replyService.get(replyId)).thenReturn(reply);
        when(authentication.getName()).thenReturn("user");

        // When
        assertThrows(InvalidReplyException.class,
            () -> webtoonCommentFacade.removeReply(authentication, webtoonId, commentId, replyId));

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).get(commentId);
        verify(replyService, times(1)).get(replyId);
        verify(reply, never()).delete();
    }

    @Test
    @DisplayName("댓글 추천 성공 테스트")
    public void testAddCommentRecommend() {
        // Given
        User user = new User();
        Webtoon webtoon = new Webtoon();
        Comment comment = Comment.builder()
            .user(user)
            .webtoon(webtoon)
            .build();
        Long webtoonId = 1L;
        Long commentId = 2L;

        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(commentService.get(commentId)).thenReturn(comment);
        when(authentication.getName()).thenReturn("user");

        // When
        webtoonCommentFacade.addCommentRecommend(authentication, webtoonId, commentId);

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).get(commentId);
        verify(commentRecommendService, times(1)).save(any(CommentRecommend.class));
    }

    @Test
    @DisplayName("웹툰ID와 댓글의 웹툰이 다를 때 댓글 추천 예외 발생 테스트")
    public void testAddCommentRecommendWhenWebtoonMismatch() {
        // Given
        User user = new User();
        User commentUser = new User();
        Webtoon webtoon = new Webtoon();
        Webtoon originWebtoon = new Webtoon();
        Comment comment = Comment.builder()
            .user(commentUser)
            .webtoon(originWebtoon)
            .build();
        Long webtoonId = 1L;
        Long commentId = 2L;

        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(commentService.get(commentId)).thenReturn(comment);
        when(authentication.getName()).thenReturn("user");

        // When
        assertThrows(InvalidCommentException.class,
            () -> webtoonCommentFacade.addCommentRecommend(authentication, webtoonId, commentId));

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).get(commentId);
        verify(commentRecommendService, never()).save(any(CommentRecommend.class));
    }

    @Test
    @DisplayName("댓글 추천 삭제 성공 테스트")
    public void testRemoveCommentRecommend() {
        // Given
        User user = new User();
        User commentUser = new User();
        Webtoon webtoon = new Webtoon();
        Comment comment = Comment.builder()
            .user(commentUser)
            .webtoon(webtoon)
            .build();
        CommentRecommend commentRecommend = CommentRecommend.builder()
            .user(user)
            .comment(comment)
            .build();
        Long webtoonId = 1L;
        Long commentId = 2L;
        Long recommendId = 3L;

        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(commentService.get(commentId)).thenReturn(comment);
        when(commentRecommendService.get(recommendId)).thenReturn(commentRecommend);
        when(authentication.getName()).thenReturn("user");

        // When
        webtoonCommentFacade.removeCommentRecommend(authentication, webtoonId, commentId,
            recommendId);

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).get(commentId);
        verify(commentRecommendService, times(1)).get(recommendId);
        verify(commentRecommendService, times(1)).delete(commentRecommend);
    }

    @Test
    @DisplayName("잘못된 사용자로 댓글 추천 삭제시 예외 발생 테스트")
    public void testRemoveCommentRecommendWithInvalidUser() {
        // Given
        User user = new User();
        User commentUser = new User();
        User originUser = new User();
        Webtoon webtoon = new Webtoon();
        Comment comment = Comment.builder()
            .user(commentUser)
            .webtoon(webtoon)
            .build();
        CommentRecommend commentRecommend = CommentRecommend.builder()
            .user(originUser)
            .comment(comment)
            .build();
        Long webtoonId = 1L;
        Long commentId = 2L;
        Long recommendId = 3L;

        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(commentService.get(commentId)).thenReturn(comment);
        when(commentRecommendService.get(recommendId)).thenReturn(commentRecommend);
        when(authentication.getName()).thenReturn("user");

        // When
        assertThrows(InvalidCommentRecommendException.class, () ->
            webtoonCommentFacade.removeCommentRecommend(authentication, webtoonId, commentId,
                recommendId));

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).get(commentId);
        verify(commentRecommendService, times(1)).get(recommendId);
        verify(commentRecommendService, never()).delete(commentRecommend);
    }

    @Test
    @DisplayName("댓글ID와 추천한 댓글이 다를 때 댓글 추천 삭제시 예외 발생 테스트")
    public void testRemoveCommentRecommendWhenCommentMismatch() {
        // Given
        User user = new User();
        User commentUser = new User();
        Webtoon webtoon = new Webtoon();
        Comment comment = Comment.builder()
            .user(commentUser)
            .webtoon(webtoon)
            .build();
        Comment originComment = Comment.builder()
            .user(commentUser)
            .webtoon(webtoon)
            .build();
        CommentRecommend commentRecommend = CommentRecommend.builder()
            .user(user)
            .comment(originComment)
            .build();
        Long webtoonId = 1L;
        Long commentId = 2L;
        Long recommendId = 3L;

        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(commentService.get(commentId)).thenReturn(comment);
        when(commentRecommendService.get(recommendId)).thenReturn(commentRecommend);
        when(authentication.getName()).thenReturn("user");

        // When
        assertThrows(InvalidCommentRecommendException.class, () ->
            webtoonCommentFacade.removeCommentRecommend(authentication, webtoonId, commentId,
                recommendId));

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).get(commentId);
        verify(commentRecommendService, times(1)).get(recommendId);
        verify(commentRecommendService, never()).delete(commentRecommend);
    }

    @Test
    @DisplayName("답글 추천 성공 테스트")
    public void testAddReplyRecommend() {
        // Given
        User user = new User();
        User commentUser = new User();
        User replyUser = new User();
        Webtoon webtoon = new Webtoon();
        Comment comment = Comment.builder()
            .user(commentUser)
            .webtoon(webtoon)
            .build();
        Reply reply = Reply.builder()
            .user(replyUser)
            .comment(comment)
            .build();
        Long webtoonId = 1L;
        Long commentId = 2L;
        Long replyId = 3L;

        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(commentService.get(commentId)).thenReturn(comment);
        when(replyService.get(replyId)).thenReturn(reply);
        when(authentication.getName()).thenReturn("user");

        // When
        webtoonCommentFacade.addReplyRecommend(authentication, webtoonId, commentId, replyId);

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).get(commentId);
        verify(replyService, times(1)).get(replyId);
        verify(replyRecommendService, times(1)).save(any(ReplyRecommend.class));
    }

    @Test
    @DisplayName("댓글ID와 답글의 댓글이 다를 때 답글 추천 시 예외 발생 테스트")
    public void testAddReplyRecommendWhenCommentMismatch() {
        // Given
        User user = new User();
        User commentUser = new User();
        User replyUser = new User();
        Webtoon webtoon = new Webtoon();
        Comment comment = Comment.builder()
            .user(commentUser)
            .webtoon(webtoon)
            .build();
        Comment originComment = Comment.builder()
            .user(commentUser)
            .webtoon(webtoon)
            .build();
        Reply reply = Reply.builder()
            .user(replyUser)
            .comment(originComment)
            .build();
        Long webtoonId = 1L;
        Long commentId = 2L;
        Long replyId = 3L;

        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(commentService.get(commentId)).thenReturn(comment);
        when(replyService.get(replyId)).thenReturn(reply);
        when(authentication.getName()).thenReturn("user");

        // When
        assertThrows(InvalidReplyException.class, () ->
            webtoonCommentFacade.addReplyRecommend(authentication, webtoonId, commentId, replyId));

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).get(commentId);
        verify(replyService, times(1)).get(replyId);
        verify(replyRecommendService, never()).save(any(ReplyRecommend.class));
    }

    @Test
    @DisplayName("웹툰ID와 답글의 웹툰이 다를 때 답글 추천 시 예외 발생 테스트")
    public void testAddReplyRecommendWhenWebtoonMismatch() {
        // Given
        User user = new User();
        User commentUser = new User();
        User replyUser = new User();
        Webtoon webtoon = new Webtoon();
        Webtoon originWebtoon = new Webtoon();
        Comment comment = Comment.builder()
            .user(commentUser)
            .webtoon(originWebtoon)
            .build();
        Reply reply = Reply.builder()
            .user(replyUser)
            .comment(comment)
            .build();
        Long webtoonId = 1L;
        Long commentId = 2L;
        Long replyId = 3L;

        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(commentService.get(commentId)).thenReturn(comment);
        when(replyService.get(replyId)).thenReturn(reply);
        when(authentication.getName()).thenReturn("user");

        // When
        assertThrows(InvalidReplyException.class, () ->
            webtoonCommentFacade.addReplyRecommend(authentication, webtoonId, commentId, replyId));

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).get(commentId);
        verify(replyService, times(1)).get(replyId);
        verify(replyRecommendService, never()).save(any(ReplyRecommend.class));
    }

    @Test
    @DisplayName("답글 추천 삭제 성공 테스트")
    public void testRemoveReplyRecommend() {
        // Given
        User user = new User();
        User commentUser = new User();
        User replyUser = new User();
        Webtoon webtoon = new Webtoon();
        Comment comment = Comment.builder()
            .user(commentUser)
            .webtoon(webtoon)
            .build();
        Reply reply = Reply.builder()
            .user(replyUser)
            .comment(comment)
            .build();
        ReplyRecommend replyRecommend = ReplyRecommend.builder()
            .user(user)
            .reply(reply)
            .build();
        Long webtoonId = 1L;
        Long commentId = 2L;
        Long replyId = 3L;
        Long recommendId = 4L;

        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(commentService.get(commentId)).thenReturn(comment);
        when(replyService.get(replyId)).thenReturn(reply);
        when(replyRecommendService.get(recommendId)).thenReturn(replyRecommend);
        when(authentication.getName()).thenReturn("user");

        // When
        webtoonCommentFacade.removeReplyRecommend(authentication, webtoonId, commentId, replyId,
            recommendId);

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).get(commentId);
        verify(replyService, times(1)).get(replyId);
        verify(replyRecommendService, times(1)).delete(replyRecommend);
    }

    @Test
    @DisplayName("잘못된 사용자로 답글 추천 삭제 시 예외 발생 테스트")
    public void testRemoveReplyRecommendWithInvalidUser() {
        // Given
        User user = new User();
        User originUser = new User();
        User commentUser = new User();
        User replyUser = new User();
        Webtoon webtoon = new Webtoon();
        Comment comment = Comment.builder()
            .user(commentUser)
            .webtoon(webtoon)
            .build();
        Reply reply = Reply.builder()
            .user(replyUser)
            .comment(comment)
            .build();
        ReplyRecommend replyRecommend = ReplyRecommend.builder()
            .user(originUser)
            .reply(reply)
            .build();
        Long webtoonId = 1L;
        Long commentId = 2L;
        Long replyId = 3L;
        Long recommendId = 4L;

        when(userService.get("user")).thenReturn(user);
        when(webtoonService.get(webtoonId)).thenReturn(webtoon);
        when(commentService.get(commentId)).thenReturn(comment);
        when(replyService.get(replyId)).thenReturn(reply);
        when(replyRecommendService.get(recommendId)).thenReturn(replyRecommend);
        when(authentication.getName()).thenReturn("user");

        // When
        assertThrows(InvalidReplyRecommendException.class,
            () -> webtoonCommentFacade.removeReplyRecommend(authentication, webtoonId, commentId,
                replyId, recommendId));

        // Then
        verify(userService, times(1)).get("user");
        verify(webtoonService, times(1)).get(webtoonId);
        verify(commentService, times(1)).get(commentId);
        verify(replyService, times(1)).get(replyId);
        verify(replyRecommendService, never()).delete(replyRecommend);
    }
}