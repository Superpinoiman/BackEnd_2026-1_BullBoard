package com.bullboard.repository;

import com.bullboard.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByArticleIdOrderByIdAsc(Long articleId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Comment c set c.member = null where c.member.id = :memberId")
    int anonymizeByMemberId(@Param("memberId") Long memberId);
}
