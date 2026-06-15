package com.zincoid.nullbot.core.module.game.impl.reversi;

import com.zincoid.nullbot.core.module.game.framework.GameLogic;
import com.zincoid.nullbot.core.module.game.model.Match;
import org.springframework.stereotype.Component;

@Component
public class ReversiLogic extends GameLogic<ReversiState> {

    private static final int[][] DIRS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
    };

    public ReversiState create(Match match) {
        ReversiState state = new ReversiState();
        state.setBlack(match.getP1().getId());
        state.setWhite(match.getP2().getId());
        return state;
    }

    public boolean place(ReversiState s, int r, int c) {
        if (!in(r, c) || s.getBoard()[r][c] != '.') return false;
        char me = s.getCurrent();
        char op = me == 'B' ? 'W' : 'B';
        boolean ok = false;
        for (int[] d : DIRS)
            ok |= check(s, r, c, d[0], d[1], me, op, false);
        if (!ok) return false;
        for (int[] d : DIRS)
            check(s, r, c, d[0], d[1], me, op, true);
        s.getBoard()[r][c] = me;
        s.setCurrent(op);
        return true;
    }

    public boolean noMoves(ReversiState s, char color) {
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                if (canPlace(s, i, j, color))
                    return false;
        return true;
    }

    private boolean canPlace(ReversiState s, int r, int c, char me) {
        if (s.getBoard()[r][c] != '.') return false;
        char op = me == 'B' ? 'W' : 'B';
        for (int[] d : DIRS)
            if (check(s, r, c, d[0], d[1], me, op, false))
                return true;
        return false;
    }

    private boolean check(ReversiState s, int r, int c,
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
