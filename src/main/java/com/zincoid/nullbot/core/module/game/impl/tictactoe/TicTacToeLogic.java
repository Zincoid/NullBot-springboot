package com.zincoid.nullbot.core.module.game.impl.tictactoe;

import com.zincoid.nullbot.core.module.game.framework.GameLogic;
import com.zincoid.nullbot.core.module.game.model.Match;
import org.springframework.stereotype.Component;

@Component
public class TicTacToeLogic extends GameLogic<TicTacToeState> {

    public TicTacToeState create(Match match) {
        TicTacToeState state = new TicTacToeState();
        state.setX(match.getP1().getId());
        state.setO(match.getP2().getId());
        return state;
    }

    public boolean place(TicTacToeState s, int r, int c) {
        if (r < 0 || r >= 3 || c < 0 || c >= 3) return false;
        if (s.getBoard()[r][c] != '.') return false;
        s.getBoard()[r][c] = s.getCurrent();
        s.setCurrent(s.getCurrent() == 'X' ? 'O' : 'X');
        return true;
    }

    public Character check(TicTacToeState s) {
        char[][] b = s.getBoard();
        for (int i = 0; i < 3; i++) {
            if (b[i][0] != '.' && b[i][0] == b[i][1] && b[i][1] == b[i][2]) return b[i][0];
            if (b[0][i] != '.' && b[0][i] == b[1][i] && b[1][i] == b[2][i]) return b[0][i];
        }
        if (b[0][0] != '.' && b[0][0] == b[1][1] && b[1][1] == b[2][2]) return b[0][0];
        if (b[0][2] != '.' && b[0][2] == b[1][1] && b[1][1] == b[2][0]) return b[0][2];
        return null;
    }

    public boolean draw(TicTacToeState s) {
        for (char[] row : s.getBoard())
            for (char c : row)
                if (c == '.') return false;
        return true;
    }
}
