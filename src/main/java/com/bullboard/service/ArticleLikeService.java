package com.bullboard.service;

import com.bullboard.domain.Article;
import com.bullboard.domain.ArticleLike;
import com.bullboard.domain.Member;
import com.bullboard.dto.ArticleLikeResponse;
import com.bullboard.exception.ApiException;
import com.bullboard.repository.ArticleLikeRepository;
import com.bullboard.repository.ArticleRepository;
import com.bullboard.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ArticleLikeService {

    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;

    public ArticleLikeService(ArticleLikeRepository articleLikeRepository,
                              ArticleRepository articleRepository,
                              MemberRepository memberRepository) {
        this.articleLikeRepository = articleLikeRepository;
        this.articleRepository = articleRepository;
        this.memberRepository = memberRepository;
    }

    public ArticleLikeResponse getLike(Long articleId, Long memberId) {
        validateArticle(articleId);
        return response(articleId, memberId);
    }

    @Transactional
    public ArticleLikeResponse like(Long articleId, Long memberId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED));

        if (!articleLikeRepository.existsByArticleIdAndMemberId(articleId, memberId)) {
            articleLikeRepository.save(new ArticleLike(article, member));
        }
        return response(articleId, memberId);
    }

    @Transactional
    public ArticleLikeResponse unlike(Long articleId, Long memberId) {
        validateArticle(articleId);
        articleLikeRepository.deleteByArticleIdAndMemberId(articleId, memberId);
        return response(articleId, memberId);
    }

    private void validateArticle(Long articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new ApiException(HttpStatus.NOT_FOUND);
        }
    }

    private ArticleLikeResponse response(Long articleId, Long memberId) {
        long count = articleLikeRepository.countByArticleId(articleId);
        boolean liked = memberId != null
                && articleLikeRepository.existsByArticleIdAndMemberId(articleId, memberId);
        return new ArticleLikeResponse(count, liked);
    }
}
