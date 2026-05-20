package com.zincoid.nullbot.core.component.game.logic;

import com.zincoid.nullbot.core.component.game.GameLogic;
import com.zincoid.nullbot.core.entity.game.tictactoe.TicTacToeGameState;
import org.springframework.stereotype.Component;

@Component
public class TicTacToeGameLogic extends GameLogic {

    public boolean place(TicTacToeGameState s, int r, int c) {
        if (r < 0 || r >= 3 || c < 0 || c >= 3) return false;
        if (s.getBoard()[r][c] != '.') return false;

        s.getBoard()[r][c] = s.getCurrentTurn();
        s.setCurrentTurn(s.getCurrentTurn() == 'X' ? 'O' : 'X');
        return true;
    }

    public Character checkWinner(TicTacToeGameState s) {
        char[][] b = s.getBoard();
        for (int i = 0; i < 3; i++) {
            if (b[i][0] != '.' && b[i][0] == b[i][1] && b[i][1] == b[i][2])
                return b[i][0];
            if (b[0][i] != '.' && b[0][i] == b[1][i] && b[1][i] == b[2][i])
                return b[0][i];
        }
        if (b[0][0] != '.' && b[0][0] == b[1][1] && b[1][1] == b[2][2])
            return b[0][0];
        if (b[0][2] != '.' && b[0][2] == b[1][1] && b[1][1] == b[2][0])
            return b[0][2];
        return null;
    }

    public boolean isDraw(TicTacToeGameState s) {
        for (char[] row : s.getBoard())
            for (char c : row)
                if (c == '.') return false;
        return true;
    }
}
