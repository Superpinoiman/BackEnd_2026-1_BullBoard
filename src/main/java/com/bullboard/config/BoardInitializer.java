package com.bullboard.config;

import com.bullboard.domain.Board;
import com.bullboard.repository.BoardRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class BoardInitializer implements ApplicationRunner {

    private static final List<String> DEFAULT_BOARD_NAMES = List.of(
            "자유게시판",
            "종목 토론",
            "투자 질문",
            "포트폴리오"
    );

    private final BoardRepository boardRepository;

    public BoardInitializer(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String boardName : DEFAULT_BOARD_NAMES) {
            if (!boardRepository.existsByName(boardName)) {
                boardRepository.save(new Board(boardName));
            }
        }
    }
}
