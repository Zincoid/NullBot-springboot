package org.bot.nullbot.entity.game.reversi;

import lombok.Data;

import java.util.Arrays;

@Data
public class ReversiGameState
{
    public static final int SIZE = 8;

    private char[][] board = new char[SIZE][SIZE];

    private Long blackPlayerId;
    private Long whitePlayerId;

    private char currentTurn; // 'B' or 'W'
    private boolean finished = false;

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
