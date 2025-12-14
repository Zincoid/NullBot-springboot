package org.bot.nullbot.component.game.logic;

import org.bot.nullbot.component.game.GameLogic;
import org.bot.nullbot.entity.game.reversi.ReversiGameState;
import org.springframework.stereotype.Component;


@Component
public class ReversiGameLogic extends GameLogic
{
    private static final int[][] DIRS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
    };

    public boolean place(ReversiGameState s, int r, int c) {
        if (!in(r, c) || s.getBoard()[r][c] != '.') return false;

        char me = s.getCurrentTurn();
        char op = me == 'B' ? 'W' : 'B';

        boolean ok = false;
        for (int[] d : DIRS)
            ok |= check(s, r, c, d[0], d[1], me, op, false);

        if (!ok) return false;

        for (int[] d : DIRS)
            check(s, r, c, d[0], d[1], me, op, true);

        s.getBoard()[r][c] = me;
        s.setCurrentTurn(op);
        return true;
    }

    public boolean hasAnyMove(ReversiGameState s, char color) {
        char old = s.getCurrentTurn();
        s.setCurrentTurn(color);
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                if (canPlace(s, i, j)) {
                    s.setCurrentTurn(old);
                    return true;
                }
        s.setCurrentTurn(old);
        return false;
    }

    private boolean canPlace(ReversiGameState s, int r, int c) {
        if (s.getBoard()[r][c] != '.') return false;
        char me = s.getCurrentTurn();
        char op = me == 'B' ? 'W' : 'B';
        for (int[] d : DIRS)
            if (check(s, r, c, d[0], d[1], me, op, false))
                return true;
        return false;
    }

    private boolean check(ReversiGameState s, int r, int c,
                          int dr, int dc, char me, char op, boolean flip) {
        int x = r + dr, y = c + dc, cnt = 0;
        while (in(x, y) && s.getBoard()[x][y] == op) {
            x += dr;
            y += dc;
            cnt++;
        }
        if (cnt > 0 && in(x, y) && s.getBoard()[x][y] == me) {
            if (flip)
                for (int i = 1; i <= cnt; i++)
                    s.getBoard()[r + dr * i][c + dc * i] = me;
            return true;
        }
        return false;
    }

    private boolean in(int r, int c) {
        return r >= 0 && r < 8 && c >= 0 && c < 8;
    }
}
