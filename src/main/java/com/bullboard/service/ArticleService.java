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
import com.bullboard.exception.ApiException;

import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    public ArticleService(ArticleRepository articleRepository,
                          BoardRepository boardRepository,
                          MemberRepository memberRepository) {
        this.articleRepository = articleRepository;
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public ArticleResponse getArticle(Long id, Long loginMemberId) {
        if (articleRepository.increaseViewCount(id) == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND);
        }

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));
        return new ArticleResponse(article, loginMemberId);
    }

    public ArticlePageResponse getArticles(Long boardId, String keyword, String sort,
                                           int page, int size,
                                           Long loginMemberId) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String safeKeyword = normalizeKeyword(keyword);
        Sort pageSort = "views".equalsIgnoreCase(sort)
                ? Sort.by(Sort.Order.desc("viewCount"), Sort.Order.desc("id"))
                : Sort.by(Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(safePage, safeSize, pageSort);
        Page<Article> articles;

        if (boardId == null) {
            articles = safeKeyword.isBlank()
                    ? articleRepository.findAll(pageable)
                    : articleRepository.searchAll(safeKeyword, pageable);
        } else {
            if (!boardRepository.existsById(boardId)) {
                throw new ApiException(HttpStatus.NOT_FOUND);
            }
            articles = safeKeyword.isBlank()
                    ? articleRepository.findByBoardId(boardId, pageable)
                    : articleRepository.searchByBoardId(boardId, safeKeyword, pageable);
        }

        Page<ArticleResponse> responsePage = articles
                .map(article -> new ArticleResponse(article, loginMemberId));
        return new ArticlePageResponse(responsePage);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        String normalized = keyword.trim();
        return normalized.length() > 100 ? normalized.substring(0, 100) : normalized;
    }

    @Transactional
    public ArticleResponse createArticle(ArticleCreateRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED));
        Board board = boardRepository.findById(request.getBoardId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST));

        Article article = new Article(board, member, request.getTitle(), request.getContent());
        articleRepository.save(article);
        return new ArticleResponse(article, memberId);
    }

    @Transactional
    public ArticleResponse updateArticle(Long id, ArticleUpdateRequest request, Long memberId) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));

        validateAuthor(article, memberId);

        Board board = boardRepository.findById(request.getBoardId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST));

        article.update(board, request.getTitle(), request.getContent());
        return new ArticleResponse(article, memberId);
    }

    @Transactional
    public void deleteArticle(Long id, Long memberId) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));
        validateAuthor(article, memberId);
        articleRepository.delete(article);
    }

    private void validateAuthor(Article article, Long memberId) {
        if (!article.getAuthor().getId().equals(memberId)) {
            throw new ApiException(HttpStatus.FORBIDDEN);
        }
    }
}
