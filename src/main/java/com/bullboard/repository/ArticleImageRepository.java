package com.bullboard.repository;

import com.bullboard.domain.ArticleImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleImageRepository extends JpaRepository<ArticleImage, Long> {

    List<ArticleImage> findByArticleIdOrderBySortOrderAsc(Long articleId);

    long countByArticleId(Long articleId);

    boolean existsByObjectKey(String objectKey);
}
