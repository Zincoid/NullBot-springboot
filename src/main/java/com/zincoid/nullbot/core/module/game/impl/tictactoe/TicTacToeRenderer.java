package com.zincoid.nullbot.core.module.game.impl.tictactoe;

import com.zincoid.nullbot.core.module.game.framework.GameRenderer;
import org.springframework.stereotype.Component;

@Component
public class TicTacToeRenderer extends GameRenderer<TicTacToeState> {

    private static final char EMPTY = '　';
    private static final char FULL_X = 'Ｘ';
    private static final char FULL_O = 'Ｏ';
    private static final char VERTICAL = '┃';
    private static final char HORIZONTAL = '━';
    private static final char CROSS = '╋';

    @Override
    public String render(TicTacToeState state) {
        return board(state.getBoard())
                + "\n当前回合："
                + (state.getCurrent() == 'X'
                ? (FULL_X + "\n" + state.getX())
                : (FULL_O + "\n" + state.getO()));
    }

    private String board(char[][] grid) {
        StringBuilder sb = new StringBuilder();
        sb.append("[井字棋]\n\n");
        for (int i = 0; i < 3; i++) {
            sb.append(" ").append(full(grid[i][0]))
                    .append(" ").append(VERTICAL).append(" ")
                    .append(full(grid[i][1]))
                    .append(" ").append(VERTICAL).append(" ")
                    .append(full(grid[i][2])).append("\n");
            if (i < 2) {
                sb.append(" ").append(HORIZONTAL).append(HORIZONTAL).append(HORIZONTAL)
                        .append(CROSS)
                        .append(HORIZONTAL).append(HORIZONTAL).append(HORIZONTAL)
                        .append(CROSS)
                        .append(HORIZONTAL).append(HORIZONTAL).append(HORIZONTAL)
                        .append("\n");
            }
        }
        return sb.toString();
    }

    private char full(char c) {
        if (c == 'X') return FULL_X;
        if (c == 'O') return FULL_O;
        return EMPTY;
    }

    public String resultMessage(char winner) {
        char fullChar = (winner == 'X') ? FULL_X : FULL_O;
        return String.format("\n\n🎉%s获胜 获得30抽数和100Exp", fullChar);
    }

    public String drawMessage() {
        return "\n\n🤝平局 双方均可获得15抽数和50Exp";
    }
}
