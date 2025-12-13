package org.bot.nullbot.component.game.extend;

import com.mikuac.shiro.core.BotContainer;
import org.springframework.beans.factory.annotation.Value;
import org.bot.nullbot.component.game.GameMatchHandler;
import org.bot.nullbot.component.game.MatchManager;
import org.bot.nullbot.entity.game.basic.GameResult;
import org.bot.nullbot.entity.game.basic.Match;
import org.bot.nullbot.entity.game.basic.Player;
import org.bot.nullbot.entity.game.reversi.ReversiGameState;
import org.bot.nullbot.component.game.logic.ReversiGameLogic;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class ReversiMatchHandler extends GameMatchHandler<ReversiGameState>
{
    private final ReversiGameLogic gameLogic;
    private final MatchManager matchManager;

    // matchId -> game state
    private final Map<String, ReversiGameState> games = new ConcurrentHashMap<>();

    public ReversiMatchHandler(
            @Value("${nullbot.bot-id}") Long botId,
            BotContainer botContainer,
            ReversiGameLogic gameLogic,
            MatchManager matchManager) {
        super(botId, botContainer);
        this.gameLogic = gameLogic;
        this.matchManager = matchManager;
    }

    @Override
    public String gameType() {
        return "reversi";
    }

    @Override
    public boolean canMatch(Player p1, Player p2) {
        return true;
    }

    @Override
    public void onMatchStart(Match match) {
        ReversiGameState state = new ReversiGameState();
        state.setBlackPlayerId(match.getPlayer1().getUserId());
        state.setWhitePlayerId(match.getPlayer2().getUserId());
        games.put(match.getMatchId(), state);
        sendInitMessage(match, state);
    }

    @Override
    public void onMatchEnd(Match match) {
        games.remove(match.getMatchId());
    }

    /**
     * 黑白棋落子
     */
    public GameResult move(Long userId, String pos) {
        Match match = matchManager.getMatchBySelfId(userId);
        if (match == null) {
            return GameResult.error("[黑白棋] ❌对局不存在");
        }

        matchManager.updateMatchStatus(match, Match.MatchStatus.PLAYING);

        ReversiGameState state = games.get(match.getMatchId());
        if (state == null) {
            return GameResult.error("[黑白棋] ❌游戏状态不存在");
        }

        if (state.isFinished()) {
            return GameResult.error("[黑白棋] ❌对局已结束");
        }

        char myColor =
                userId.equals(state.getBlackPlayerId()) ? 'B' :
                        userId.equals(state.getWhitePlayerId()) ? 'W' : 0;

        if (state.getCurrentTurn() != myColor) {
            return GameResult.error("[黑白棋] ⏳还没轮到你下棋");
        }

        int col = pos.charAt(0) - 'A';
        int row = pos.charAt(1) - '1';

        if (!gameLogic.place(state, row, col)) {
            return GameResult.error("[黑白棋] ❌非法落子");
        }

        // ===== 构造返回文本 =====
        StringBuilder info = new StringBuilder();
        info.append(render(state));

        // 判定是否结束
        if (!gameLogic.hasAnyMove(state, 'B')
                && !gameLogic.hasAnyMove(state, 'W')) {

            state.setFinished(true);
            info.append("\n").append(judge(state));
        }

        return getGameResult(userId, match, info.toString());
    }

    // ================== 工具方法 ==================

    private static final String[] COLS = {
            "🅰️", "🅱️", "🅲", "🅳", "🅴", "🅵", "🅶", "🅷"
    };

    private static final String[] ROWS = {
            "1⃣️", "2⃣️", "3⃣️", "4⃣️",
            "5⃣️", "6⃣️", "7⃣️", "8⃣️"
    };

    @Override
    protected String render(ReversiGameState s) {
        StringBuilder sb = new StringBuilder();
        sb.append("【黑白棋】\n");

        // 列头
        sb.append("⬛");
        for (String col : COLS) {
            sb.append(col);
        }
        sb.append("\n");

        // 棋盘
        for (int i = 0; i < 8; i++) {
            sb.append(ROWS[i]);
            for (int j = 0; j < 8; j++) {
                char cell = s.getBoard()[i][j];
                if (cell == 'B') {
                    sb.append("⚫");
                } else if (cell == 'W') {
                    sb.append("⚪");
                } else {
                    sb.append("⬜");
                }
            }
            sb.append("\n");
        }

        sb.append("当前回合：")
                .append(s.getCurrentTurn() == 'B' ? "⚫ 黑棋" : "⚪ 白棋");

        return sb.toString();
    }

    private String judge(ReversiGameState s) {
        int b = 0, w = 0;
        for (char[] row : s.getBoard())
            for (char c : row)
                if (c == 'B') b++;
                else if (c == 'W') w++;

        if (b > w) return "🎉 黑棋胜利！(" + b + " : " + w + ")";
        if (w > b) return "🎉 白棋胜利！(" + w + " : " + b + ")";
        return "🤝 平局！(" + b + " : " + w + ")";
    }
}
