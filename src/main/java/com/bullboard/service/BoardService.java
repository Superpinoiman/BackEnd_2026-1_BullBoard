package com.bullboard.service;

import com.bullboard.repository.BoardRepository;
import com.bullboard.domain.Board;
import com.bullboard.dto.BoardRequest;
import com.bullboard.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Transactional
    public Board createBoard(BoardRequest request) {
        Board board = new Board(request.getName());
        return boardRepository.save(board);
    }

    public Board getBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));
        return board;
    }

    public List<Board> getBoards() {
        return boardRepository.findAll();
    }

    @Transactional
    public Board updateBoard(Long id, BoardRequest request) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));

        board.update(request.getName());
        return board;
    }

    @Transactional
    public void deleteBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));

        boardRepository.delete(board);
    }

}
