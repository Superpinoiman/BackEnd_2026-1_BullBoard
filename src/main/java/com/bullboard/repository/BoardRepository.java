package com.bullboard.repository;

import com.bullboard.domain.Board;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {

    boolean existsByName(String name);
}
