package org.bot.nullbot.component.game.extend;

import com.mikuac.shiro.core.BotContainer;
import org.bot.nullbot.component.game.GameMatchHandler;
import org.bot.nullbot.component.game.MatchManager;
import org.bot.nullbot.component.game.logic.TicTacToeGameLogic;
import org.bot.nullbot.entity.game.basic.GameResult;
import org.bot.nullbot.entity.game.basic.Match;
import org.bot.nullbot.entity.game.basic.Player;
import org.bot.nullbot.entity.game.tictactoe.TicTacToeGameState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TicTacToeMatchHandler extends GameMatchHandler<TicTacToeGameState>
{
    private final TicTacToeGameLogic gameLogic;
    private final MatchManager matchManager;

    private final Map<String, TicTacToeGameState> games = new ConcurrentHashMap<>();

    public TicTacToeMatchHandler(
            @Value("${nullbot.bot-id}") Long botId,
            BotContainer botContainer,
            TicTacToeGameLogic gameLogic,
            MatchManager matchManager) {
        super(botId, botContainer);
        this.gameLogic = gameLogic;
        this.matchManager = matchManager;
    }

    @Override
    public String gameType() {
        return "tictactoe";
    }

    @Override
    public boolean canMatch(Player p1, Player p2) {
        return true;
    }

    @Override
    public void onMatchStart(Match match) {
        TicTacToeGameState state = new TicTacToeGameState();
        state.setPlayerX(match.getPlayer1().getUserId());
        state.setPlayerO(match.getPlayer2().getUserId());
        games.put(match.getMatchId(), state);
        sendInitMessage(match, state);
    }

    @Override
    public void onMatchEnd(Match match) {
        games.remove(match.getMatchId());
    }

    public GameResult move(Long userId, int r, int c) {
        Match match = matchManager.getMatchBySelfId(userId);
        if (match == null) {
            return GameResult.error("[井字棋] ❌对局不存在");
        }

        TicTacToeGameState state = games.get(match.getMatchId());
        if (state == null) {
            return GameResult.error("[井字棋] ❌游戏状态不存在");
        }

        char my =
                userId.equals(state.getPlayerX()) ? 'X' :
                        userId.equals(state.getPlayerO()) ? 'O' : 0;

        if (my == 0) {
            return GameResult.error("[井字棋] ❌你不是该对局玩家");
        }

        if (state.getCurrentTurn() != my) {
            return GameResult.error("[井字棋] ⏳还没轮到你");
        }

        if (!gameLogic.place(state, r, c)) {
            return GameResult.error("[井字棋] ❌非法落子");
        }

        StringBuilder info = new StringBuilder();
        info.append(render(state));

        Character winner = gameLogic.checkWinner(state);
        if (winner != null) {
            state.setFinished(true);
            info.append("\n🎉 ")
                    .append(winner == 'X' ? "X" : "O")
                    .append(" 获胜！");
        } else if (gameLogic.isDraw(state)) {
            state.setFinished(true);
            info.append("\n🤝 平局！");
        }

        return getGameResult(userId, match, info.toString());
    }

    @Override
    protected String render(TicTacToeGameState s) {
        return printBoard(s.getBoard())
                + "\n当前回合："
                + (s.getCurrentTurn() == 'X' ? "Ｘ" : "Ｏ");
    }

    // 文本形式的棋盘
    private String printBoard(char[][] b) {
        StringBuilder sb = new StringBuilder();
        sb.append("[井字棋]\n");

        for (int i = 0; i < 3; i++) {
            char c1 = toFullWidth(b[i][0]);
            char c2 = toFullWidth(b[i][1]);
            char c3 = toFullWidth(b[i][2]);

            sb.append(" ")
                    .append(c1).append(" │ ")
                    .append(c2).append(" │ ")
                    .append(c3).append("\n");

            if (i < 2) {
                sb.append("----+----+----\n");
            }
        }
        return sb.toString();
    }

    // 将半角字符转换为全角字符
    private char toFullWidth(char c) {
        if (c == 'X' || c == 'x') {
            return 'Ｘ';
        } else if (c == 'O' || c == 'o') {
            return 'Ｏ';
        } else {
            return '　'; // 全角空格
        }
    }

}
