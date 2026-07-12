package com.bullboard.service;

import com.bullboard.repository.ArticleRepository;
import com.bullboard.repository.BoardRepository;
import com.bullboard.domain.Article;
import com.bullboard.domain.Board;
import com.bullboard.dto.PostResponse;
import com.bullboard.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {

    private final ArticleRepository articleRepository;
    private final BoardRepository boardRepository;

    public PostService(ArticleRepository articleRepository,
                       BoardRepository boardRepository) {
        this.articleRepository = articleRepository;
        this.boardRepository = boardRepository;
    }

    public List<PostResponse> getPosts(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));

        List<Article> articles = articleRepository.findByBoardIdOrderByIdDesc(boardId);
        List<PostResponse> result = new ArrayList<>();

        for (Article article : articles) {
            String authorName = article.getAuthor() == null
                    ? "알 수 없음" : article.getAuthor().getNickname();

            result.add(new PostResponse(
                    article.getId(),
                    article.getTitle(),
                    article.getContent(),
                    authorName,
                    board.getName(),
                    article.getCreatedDate()
            ));
        }

        return result;
    }
}
