package com.zincoid.nullbot.core.model.game.state.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.zincoid.nullbot.core.model.game.state.GameState;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicTacToeGameState extends GameState {

    private char[][] board = new char[3][3];

    private Long playerX;
    private Long playerO;
    private Long winnerId;

    private char currentTurn = 'X';
    private boolean finished = false;

    public TicTacToeGameState() {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                board[i][j] = '.';
    }
}
