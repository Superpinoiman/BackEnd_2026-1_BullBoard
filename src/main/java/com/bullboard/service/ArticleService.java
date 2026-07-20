package com.bullboard.service;

import com.bullboard.repository.ArticleRepository;
import com.bullboard.repository.BoardRepository;
import com.bullboard.repository.MemberRepository;
import com.bullboard.domain.Article;
import com.bullboard.domain.Board;
import com.bullboard.domain.Member;
import com.bullboard.dto.ArticleCreateRequest;
import com.bullboard.dto.ArticlePageResponse;
import com.bullboard.dto.ArticleResponse;
import com.bullboard.dto.ArticleUpdateRequest;
import com.bullboard.dto.TrendingArticleResponse;
import com.bullboard.exception.ApiException;

import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final ArticleImageService articleImageService;

    public ArticleService(ArticleRepository articleRepository,
                          BoardRepository boardRepository,
                          MemberRepository memberRepository,
                          ArticleImageService articleImageService) {
        this.articleRepository = articleRepository;
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
        this.articleImageService = articleImageService;
    }

    @Transactional
    public ArticleResponse getArticle(Long id, Long loginMemberId) {
        if (articleRepository.increaseViewCount(id) == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND);
        }

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));

        return new ArticleResponse(article, loginMemberId,
                articleImageService.getResponses(article.getId()));
    }

    public ArticlePageResponse getArticles(Long boardId, String keyword, String symbol,
                                           String sort,
                                           int page, int size,
                                           Long loginMemberId) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String safeKeyword = normalizeKeyword(keyword);
        String safeSymbol = normalizeSymbol(symbol);

        Sort pageSort = "views".equalsIgnoreCase(sort)
                ? Sort.by(Sort.Order.desc("viewCount"), Sort.Order.desc("id"))
                : Sort.by(Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(safePage, safeSize, pageSort);

        if (boardId != null && !boardRepository.existsById(boardId)) {
            throw new ApiException(HttpStatus.NOT_FOUND);
        }

        Page<Article> articles = articleRepository.search(
                boardId, safeKeyword, safeSymbol, pageable);

        Page<ArticleResponse> responsePage = articles
                .map(article -> new ArticleResponse(article, loginMemberId));

        return new ArticlePageResponse(responsePage);
    }

    public ArticlePageResponse getMyArticles(Long memberId, int page, int size) {
        return getMemberArticles(memberId, page, size, memberId);
    }

    public ArticlePageResponse getPublicArticles(Long memberId, int page, int size) {
        return getMemberArticles(memberId, page, size, null);
    }

    private ArticlePageResponse getMemberArticles(Long memberId, int page, int size,
                                                   Long loginMemberId) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(
                safePage, safeSize, Sort.by(Sort.Order.desc("id")));
        Page<ArticleResponse> responsePage = articleRepository
                .findByMemberId(memberId, pageable)
                .map(article -> new ArticleResponse(article, loginMemberId));
        return new ArticlePageResponse(responsePage);
    }

    public List<TrendingArticleResponse> getTrendingArticles(int size) {
        int safeSize = Math.min(Math.max(size, 1), 10);

        return articleRepository.findTrending(
                LocalDateTime.now().minusDays(7), PageRequest.of(0, safeSize));
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        String normalized = keyword.trim();

        return normalized.length() > 100 ? normalized.substring(0, 100) : normalized;
    }

    private String normalizeSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return "";
        }
        String normalized = symbol.trim().toUpperCase();

        if (!normalized.matches("^[A-Z][A-Z0-9.-]{0,9}$")) {
            throw new ApiException(HttpStatus.BAD_REQUEST);
        }

        return normalized;
    }

    @Transactional
    public ArticleResponse createArticle(ArticleCreateRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED));
        Board board = boardRepository.findById(request.getBoardId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST));

        Article article = new Article(board, member, normalizeSymbol(request.getSymbol()),
                request.getTitle(), request.getContent());
        articleRepository.save(article);

        return new ArticleResponse(article, memberId,
                articleImageService.attachImages(
                        article, memberId, request.getImageKeys()));
    }

    @Transactional
    public ArticleResponse updateArticle(Long id, ArticleUpdateRequest request, Long memberId) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));

        validateAuthor(article, memberId);

        Board board = boardRepository.findById(request.getBoardId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST));

        article.update(board, normalizeSymbol(request.getSymbol()),
                request.getTitle(), request.getContent());
        return new ArticleResponse(article, memberId,
                articleImageService.attachImages(
                        article, memberId, request.getImageKeys()));
    }

    @Transactional
    public void deleteArticle(Long id, Long memberId) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));
        validateAuthor(article, memberId);
        articleImageService.deleteArticleImagesAfterCommit(article.getId());
        articleRepository.delete(article);
    }

    private void validateAuthor(Article article, Long memberId) {
        if (article.getAuthor() == null || !article.getAuthor().getId().equals(memberId)) {
            throw new ApiException(HttpStatus.FORBIDDEN);
        }
    }
}
