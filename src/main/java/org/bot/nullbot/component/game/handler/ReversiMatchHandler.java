package org.bot.nullbot.component.game.handler;

import com.mikuac.shiro.core.BotContainer;
import org.bot.nullbot.component.game.manager.PlayerManager;
import org.bot.nullbot.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.bot.nullbot.component.game.GameMatchHandler;
import org.bot.nullbot.component.game.manager.MatchManager;
import org.bot.nullbot.entity.result.GameResult;
import org.bot.nullbot.entity.game.basic.Match;
import org.bot.nullbot.entity.game.reversi.ReversiGameState;
import org.bot.nullbot.component.game.logic.ReversiGameLogic;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReversiMatchHandler extends GameMatchHandler<ReversiGameState, ReversiGameLogic> {

    private final UserService userService;

    public ReversiMatchHandler(
            @Value("${nullbot.bot-id}") Long botId,
            BotContainer botContainer,
            MatchManager matchManager,
            PlayerManager playerManager,
            UserService userService,
            ReversiGameLogic gameLogic)
    {
        super(botId, botContainer, matchManager, playerManager, gameLogic, new ConcurrentHashMap<>());
        this.userService = userService;
    }

    @Override
    public String gameType() {
        return "黑白棋";
    }

    @Override
    public void onMatchStart(Match match) {
        ReversiGameState state = new ReversiGameState();
        state.setBlackPlayerId(match.getPlayer1().getUserId());
        state.setWhitePlayerId(match.getPlayer2().getUserId());
        games.put(match.getMatchId(), state);

        super.onMatchStart(match);
        sendInitMessage(match, state);
    }

    @Override
    public void onMatchEnd(Match match) {
        ReversiGameState state = games.get(match.getMatchId());
        // 黑白棋 奖励逻辑
        if (state.isFinished()) {
            if (state.getWinnerId() != null) {
                userService.plusExperience(state.getWinnerId(), 200);
                userService.increaseDrawTimes(state.getWinnerId(), 50);
            } else {
                userService.plusExperience(state.getBlackPlayerId(), 100);
                userService.plusExperience(state.getWhitePlayerId(), 100);
                userService.increaseDrawTimes(state.getBlackPlayerId(), 25);
                userService.increaseDrawTimes(state.getWhitePlayerId(), 25);
            }
        }
        super.onMatchEnd(match);
    }

    /**
     * 黑白棋落子 (用户调用)
     */
    public GameResult move(Long userId, String pos) {
        Match match = matchManager.getMatchBySelfId(userId);
        if (match == null) return getErrorResult("[黑白棋] ❌对局不存在");

        ReversiGameState state = games.get(match.getMatchId());
        if (state == null) return getErrorResult("[黑白棋] ❌游戏状态不存在");
        if (state.isFinished()) return getErrorResult("[黑白棋] ❌对局已结束");

        // matchManager.updateMatchStatus(match, Match.MatchStatus.PLAYING);

        char myColor = userId.equals(state.getBlackPlayerId()) ? 'B' :
                userId.equals(state.getWhitePlayerId()) ? 'W' : 0;

        if (state.getCurrentTurn() != myColor) return getErrorResult("[黑白棋] ⏳还没轮到你下棋");

        int col = pos.charAt(0) - 'A';
        int row = pos.charAt(1) - '1';

        if (!gameLogic.place(state, row, col)) return getErrorResult("[黑白棋] ❌非法落子");

        StringBuilder info = new StringBuilder();
        info.append(render(state));

        if (!gameLogic.hasAnyMove(state, 'B') && !gameLogic.hasAnyMove(state, 'W')) {
            state.setFinished(true);
            info.append("\n").append(judge(state));
            return getFinishResult(userId, match, false, info.toString(),  null);
        }
        return getSuccessResult(userId, match, false, info.toString(), null);
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
                .append(s.getCurrentTurn() == 'B' ? ("⚫ 黑棋\n" + s.getBlackPlayerId()) : ("⚪ 白棋\n" + s.getWhitePlayerId()));

        return sb.toString();
    }

    private String judge(ReversiGameState s) {
        int b = 0, w = 0;
        for (char[] row : s.getBoard())
            for (char c : row)
                if (c == 'B') b++;
                else if (c == 'W') w++;

        if (b > w) {
            s.setWinnerId(s.getBlackPlayerId());
            return "🎉 黑棋胜利！(" + b + " : " + w + ")\n" + s.getBlackPlayerId() + " 获得50抽数和200Exp！";
        }
        if (w > b) {
            s.setWinnerId(s.getWhitePlayerId());
            return "🎉 白棋胜利！(" + w + " : " + b + ")\n" + s.getWhitePlayerId() + " 获得50抽数和200Exp！";
        }
        return "🤝 平局！(" + b + " : " + w + ")\n双方分别获得25抽数和100Exp！";
    }
}
