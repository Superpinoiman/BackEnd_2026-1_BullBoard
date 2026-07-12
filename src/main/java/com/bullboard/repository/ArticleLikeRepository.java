package com.bullboard.repository;

import com.bullboard.domain.ArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {

    long countByArticleId(Long articleId);

    boolean existsByArticleIdAndMemberId(Long articleId, Long memberId);

    void deleteByArticleIdAndMemberId(Long articleId, Long memberId);
}
