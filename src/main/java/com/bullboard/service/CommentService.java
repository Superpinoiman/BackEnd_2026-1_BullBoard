package com.bullboard.service;

import com.bullboard.domain.Article;
import com.bullboard.domain.Comment;
import com.bullboard.domain.Member;
import com.bullboard.dto.CommentRequest;
import com.bullboard.dto.CommentResponse;
import com.bullboard.exception.ApiException;
import com.bullboard.repository.ArticleRepository;
import com.bullboard.repository.CommentRepository;
import com.bullboard.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;

    public CommentService(CommentRepository commentRepository,
                          ArticleRepository articleRepository,
                          MemberRepository memberRepository) {
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
        this.memberRepository = memberRepository;
    }

    public List<CommentResponse> getComments(Long articleId, Long loginMemberId) {
        if (!articleRepository.existsById(articleId)) {
            throw new ApiException(HttpStatus.NOT_FOUND);
        }
        return commentRepository.findByArticleIdOrderByIdAsc(articleId).stream()
                .map(comment -> new CommentResponse(comment, loginMemberId))
                .toList();
    }

    @Transactional
    public CommentResponse createComment(Long articleId, CommentRequest request, Long memberId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED));

        Comment comment = commentRepository.save(
                new Comment(article, member, request.getContent().trim()));
        return new CommentResponse(comment, memberId);
    }

    @Transactional
    public CommentResponse updateComment(Long id, CommentRequest request, Long memberId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));
        validateAuthor(comment, memberId);
        comment.update(request.getContent().trim());
        return new CommentResponse(comment, memberId);
    }

    @Transactional
    public void deleteComment(Long id, Long memberId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));
        validateAuthor(comment, memberId);
        commentRepository.delete(comment);
    }

    private void validateAuthor(Comment comment, Long memberId) {
        if (comment.getMember() == null || !comment.getMember().getId().equals(memberId)) {
            throw new ApiException(HttpStatus.FORBIDDEN);
        }
    }
}
