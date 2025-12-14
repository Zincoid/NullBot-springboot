package org.bot.nullbot.entity.game.tictactoe;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bot.nullbot.entity.game.GameState;

@EqualsAndHashCode(callSuper = true)
@Data
public class TicTacToeGameState extends GameState
{
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
