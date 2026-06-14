package com.zincoid.nullbot.core.module.game.state;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Arrays;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReversiGameState  extends GameState {

    public static final int SIZE = 8;

    private char[][] board = new char[SIZE][SIZE];

    private Long blackPlayerId;
    private Long whitePlayerId;
    private Long winnerId;

    private char currentTurn; // 'B' or 'W'
    @Getter(AccessLevel.NONE)
    private boolean finished = false;

    @Override
    public boolean isFinished() {
        return finished;
    }

    public ReversiGameState() {
        init();
    }

    private void init() {
        for (int i = 0; i < SIZE; i++) {
            Arrays.fill(board[i], '.');
        }
        board[3][3] = 'W';
        board[3][4] = 'B';
        board[4][3] = 'B';
        board[4][4] = 'W';
        currentTurn = 'B'; // 黑棋先手
    }
}
