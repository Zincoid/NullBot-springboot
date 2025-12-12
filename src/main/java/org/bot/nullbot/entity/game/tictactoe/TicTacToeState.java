package org.bot.nullbot.entity.game.tictactoe;

import lombok.Data;

import java.util.Arrays;

@Data
public class TicTacToeState
{
    private char[][] board = new char[3][3];
    private Long currentPlayerId; // 当前应落子玩家的 userId

    public TicTacToeState(Long firstPlayerId) {
        this.currentPlayerId = firstPlayerId;
        for (int i = 0; i < 3; i++) {
            Arrays.fill(board[i], ' ');
        }
    }
}
