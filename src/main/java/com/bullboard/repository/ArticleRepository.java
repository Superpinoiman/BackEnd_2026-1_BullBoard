package com.bullboard.repository;

import com.bullboard.domain.Article;
import com.bullboard.dto.TrendingArticleResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("""
            select a from Article a
            where (:boardId is null or a.board.id = :boardId)
              and (
                  :keyword = ''
                  or lower(a.title) like lower(concat('%', :keyword, '%'))
                  or lower(a.content) like lower(concat('%', :keyword, '%'))
              )
              and (:symbol = '' or upper(a.symbol) = :symbol)
            """)
    Page<Article> search(@Param("boardId") Long boardId,
                         @Param("keyword") String keyword,
                         @Param("symbol") String symbol,
                         Pageable pageable);

    @Query("""
            select new com.bullboard.dto.TrendingArticleResponse(a, count(articleLike))
            from Article a
            left join a.likes articleLike
            where a.createdDate >= :since
            group by a
            order by count(articleLike) desc, a.createdDate desc, a.id desc
            """)
    List<TrendingArticleResponse> findTrending(@Param("since") LocalDateTime since,
                                                Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Article a set a.viewCount = a.viewCount + 1 where a.id = :id")
    int increaseViewCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Article a set a.member = null where a.member.id = :memberId")
    int anonymizeByMemberId(@Param("memberId") Long memberId);

}
