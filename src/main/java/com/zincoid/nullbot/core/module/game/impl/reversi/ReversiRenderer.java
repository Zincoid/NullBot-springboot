package com.zincoid.nullbot.core.module.game.impl.reversi;

import com.zincoid.nullbot.core.module.game.framework.GameRenderer;
import org.springframework.stereotype.Component;

@Component
public class ReversiRenderer extends GameRenderer<ReversiState> {

    private static final String[] COLS = {
            "🅰️", "🅱️", "🅲", "🅳", "🅴", "🅵", "🅶", "🅷"
    };

    private static final String[] ROWS = {
            "1⃣️", "2⃣️", "3⃣️", "4⃣️",
            "5⃣️", "6⃣️", "7⃣️", "8⃣️"
    };

    @Override
    public String render(ReversiState state) {
        StringBuilder sb = new StringBuilder();
        sb.append("[黑白棋]\n");
        sb.append("⬛");
        for (String col : COLS) sb.append(col);
        sb.append("\n");
        for (int i = 0; i < 8; i++) {
            sb.append(ROWS[i]);
            for (int j = 0; j < 8; j++) {
                char cell = state.getBoard()[i][j];
                if (cell == 'B') sb.append("⚫");
                else if (cell == 'W') sb.append("⚪");
                else sb.append("⬜");
            }
            sb.append("\n");
        }
        sb.append("当前回合：")
                .append(state.getCurrent() == 'B'
                        ? ("⚫黑棋\n" + state.getBlack())
                        : ("⚪白棋\n" + state.getWhite()));
        return sb.toString();
    }

    public String resultMessage(int b, int w, Long blackId, Long whiteId) {
        if (b > w) {
            return """
                    \n🎉黑棋胜利！(%d : %d)
                    %d 获得50抽数和200Exp
                    """.formatted(b, w, blackId);
        } else if (w > b) {
            return """
                    \n🎉白棋胜利(%d : %d)
                    %d 获得50抽数和200Exp
                    """.formatted(w, b, whiteId);
        } else {
            return """
                    \n🤝平局(%d : %d)
                    双方分别获得25抽数和100Exp
                    """.formatted(b, w);
        }
    }
}
