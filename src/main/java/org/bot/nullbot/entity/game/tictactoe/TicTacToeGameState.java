package org.bot.nullbot.entity.game.tictactoe;

import lombok.Data;

@Data
public class TicTacToeGameState {

    private char[][] board = new char[3][3];

    private Long playerX;
    private Long playerO;

    private char currentTurn = 'X';
    private boolean finished = false;

    public TicTacToeGameState() {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                board[i][j] = '.';
    }
}
