package org.bot.nullbot.service.game;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.component.game.MatchManager;
import org.bot.nullbot.component.game.Matcher;
import org.bot.nullbot.entity.game.basic.Match;
import org.bot.nullbot.entity.game.tictactoe.TicTacToeState;
import org.bot.nullbot.component.game.impl.TicTacToeStateHandler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TicTacToeService
{
    private final TicTacToeStateHandler handler;
    private final MatchManager matchManager;
    private final Matcher matcher;

    public String move(Long userId, int x, int y) {
        Match match = matchManager.getMatchByPlayerId(userId);
        if (match == null) { return "对局不存在"; }
        match.setLastActionTime(LocalDateTime.now());

        String matchId = match.getMatchId();
        TicTacToeState state = handler.getState(matchId);
        if (state == null) { return "对局状态不存在"; }

        if (!Objects.equals(state.getCurrentPlayerId(), userId)) { return "还没轮到你下棋！"; }
        if (x < 1 || x > 3 || y < 1 || y > 3) { return "落子范围 1-3，例如：/TicTacToe 1 3"; }
        if (state.getBoard()[x - 1][y - 1] != ' ') { return "此位置已有棋子！"; }

        char piece = (match.getPlayer1().getUserId().equals(userId)) ? 'X' : 'O';
        state.getBoard()[x - 1][y - 1] = piece;

        // 判断胜负
        if (checkWin(state.getBoard(), piece)) {
            handler.getState(matchId); // Optional, maybe update
            matcher.finishMatch(matchId);
            return printBoard(state.getBoard()) + "\n玩家 " + piece + " 获胜！对局已结束。";
        }

        // 判断平局
        if (isDraw(state.getBoard())) {
            matcher.finishMatch(matchId);
            return printBoard(state.getBoard()) + "\n平局！对局已结束。";
        }

        // 切换玩家
        Long next = match.getPlayer1().getUserId().equals(userId)
                ? match.getPlayer2().getUserId()
                : match.getPlayer1().getUserId();

        state.setCurrentPlayerId(next);

        return printBoard(state.getBoard()) +
                "\n落子成功！轮到玩家：" + next;
    }

    private boolean checkWin(char[][] b, char p) {
        for (int i = 0; i < 3; i++) {
            if ((b[i][0] == p && b[i][1] == p && b[i][2] == p) ||
                    (b[0][i] == p && b[1][i] == p && b[2][i] == p)) {
                return true;
            }
        }
        return (b[0][0] == p && b[1][1] == p && b[2][2] == p) ||
                (b[0][2] == p && b[1][1] == p && b[2][0] == p);
    }

    private boolean isDraw(char[][] b) {
        for (char[] row : b) {
            for (char c : row) {
                if (c == ' ') return false;
            }
        }
        return true;
    }

    // 文本形式的棋盘
    private String printBoard(char[][] b) {
        StringBuilder sb = new StringBuilder();
        sb.append("[井字棋]\n");

        // 使用全角字符和Unicode制表符确保对齐
        for (int i = 0; i < 3; i++) {
            // 将半角字符转换为全角字符
            char c1 = toFullWidth(b[i][0]);
            char c2 = toFullWidth(b[i][1]);
            char c3 = toFullWidth(b[i][2]);

            sb.append(" ")
                    .append(c1).append(" │ ")  // 使用全角竖线
                    .append(c2).append(" │ ")
                    .append(c3).append("\n");

            if (i < 2) {
                sb.append("----+----+----\n");  // 使用全角横线和交叉符
            }
        }
        return sb.toString();
    }

    // 将半角字符转换为全角字符
    private char toFullWidth(char c) {
        if (c == 'X' || c == 'x') {
            return 'Ｘ';  // 全角X
        } else if (c == 'O' || c == 'o') {
            return 'Ｏ';  // 全角O
        } else {
            return '　';  // 全角空格
        }
    }

    // private String printBoard(char[][] b) {
    //     StringBuilder sb = new StringBuilder();
    //     sb.append("[TicTacToe]\n");
    //     for (int i = 0; i < 3; i++) {
    //         sb.append(" ")
    //                 .append(b[i][0]).append(" | ")
    //                 .append(b[i][1]).append(" | ")
    //                 .append(b[i][2]).append("\n");
    //         if (i < 2) sb.append("---+---+---\n");
    //     }
    //     return sb.toString();
    // }
}
