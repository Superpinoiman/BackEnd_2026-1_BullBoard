package com.bullboard.repository;

import com.bullboard.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findByBoardIdOrderByIdDesc(Long boardId);

    Page<Article> findByBoardId(Long boardId, Pageable pageable);

    @Query("""
            select a from Article a
            where a.board.id = :boardId
              and (
                  lower(a.title) like lower(concat('%', :keyword, '%'))
                  or lower(a.content) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<Article> searchByBoardId(@Param("boardId") Long boardId,
                                  @Param("keyword") String keyword,
                                  Pageable pageable);

    @Query("""
            select a from Article a
            where lower(a.title) like lower(concat('%', :keyword, '%'))
               or lower(a.content) like lower(concat('%', :keyword, '%'))
            """)
    Page<Article> searchAll(@Param("keyword") String keyword, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Article a set a.viewCount = a.viewCount + 1 where a.id = :id")
    int increaseViewCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Article a set a.member = null where a.member.id = :memberId")
    int anonymizeByMemberId(@Param("memberId") Long memberId);

    boolean existsByBoardId(Long boardId);
}
