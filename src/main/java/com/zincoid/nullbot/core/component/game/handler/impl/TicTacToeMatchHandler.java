package com.zincoid.nullbot.core.component.game.handler.impl;

import com.mikuac.shiro.core.BotContainer;
import com.zincoid.nullbot.core.component.game.handler.GameMatchHandler;
import com.zincoid.nullbot.core.component.game.manager.MatchManager;
import com.zincoid.nullbot.core.component.game.manager.PlayerManager;
import com.zincoid.nullbot.core.component.game.logic.impl.TicTacToeGameLogic;
import com.zincoid.nullbot.core.model.result.GameResult;
import com.zincoid.nullbot.core.model.game.basic.Match;
import com.zincoid.nullbot.core.model.game.basic.state.impl.TicTacToeGameState;
import com.zincoid.nullbot.core.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class TicTacToeMatchHandler extends GameMatchHandler<TicTacToeGameState, TicTacToeGameLogic> {

    private final UserService userService;

    public TicTacToeMatchHandler(
            @Value("${nullbot.bot-id}") Long botId,
            BotContainer botContainer,
            MatchManager matchManager,
            PlayerManager playerManager,
            UserService userService,
            TicTacToeGameLogic gameLogic)
    {
        super(botId, botContainer, matchManager, playerManager, gameLogic, new ConcurrentHashMap<>());
        this.userService = userService;
    }

    @Override
    public String gameType() {
        return "井字棋";
    }

    @Override
    public void onMatchStart(Match match) {
        TicTacToeGameState state = new TicTacToeGameState();
        state.setPlayerX(match.getPlayer1().getUserId());
        state.setPlayerO(match.getPlayer2().getUserId());
        games.put(match.getMatchId(), state);

        super.onMatchStart(match);
        sendInitMessage(match, state);
    }

    @Override
    public void onMatchEnd(Match match) {
        TicTacToeGameState state = games.get(match.getMatchId());
        // 井字棋 奖励逻辑
        if (state.isFinished()) {
            if (state.getWinnerId() != null) {
                userService.plusExperience(state.getWinnerId(), 100);
                userService.increaseDrawTimes(state.getWinnerId(), 30);
            } else {
                userService.plusExperience(state.getPlayerO(), 50);
                userService.plusExperience(state.getPlayerX(), 50);
                userService.increaseDrawTimes(state.getPlayerO(), 15);
                userService.increaseDrawTimes(state.getPlayerX(), 15);
            }
        }
        super.onMatchEnd(match);
    }

    /**
     * 井字棋落子 (用户调用)
     */
    public GameResult move(Long userId, int r, int c) {
        Match match = matchManager.getMatchBySelfId(userId);
        if (match == null) return getErrorResult("[井字棋] ❌对局不存在");
        TicTacToeGameState state = games.get(match.getMatchId());
        if (state == null) return getErrorResult("[井字棋] ❌游戏状态不存在");

        // matchManager.updateMatchStatus(match, Match.MatchStatus.PLAYING);

        char my = userId.equals(state.getPlayerX()) ? 'X' :
                        userId.equals(state.getPlayerO()) ? 'O' : 0;

        if (my == 0) return getErrorResult("[井字棋] ❌你不是该对局玩家");
        if (state.getCurrentTurn() != my) return getErrorResult("[井字棋] ⏳还没轮到你");
        if (!gameLogic.place(state, r, c)) return getErrorResult("[井字棋] ❌非法落子");

        StringBuilder info = new StringBuilder();
        info.append(render(state));

        Character winner = gameLogic.checkWinner(state);
        if (winner != null) {
            state.setFinished(true);
            state.setWinnerId(winner == 'X' ? state.getPlayerX() : state.getPlayerO());
            info.append("\n\n🎉 ")
                    .append(winner == 'X' ? "X" : "O")
                    .append(" 获胜！获得30抽数和100Exp！");
            return getFinishResult(userId, match, false, info.toString(), null);
        } else if (gameLogic.isDraw(state)) {
            state.setFinished(true);
            info.append("\n\n🤝 平局！\n 双方均可获得15抽数和50Exp！");
            return getFinishResult(userId, match, false, info.toString(), null);
        }
        return getSuccessResult(userId, match, false, info.toString(), null);
    }

    // ================== 工具方法 ==================

    @Override
    protected String render(TicTacToeGameState s) {
        return printBoard(s.getBoard())
                + "\n当前回合："
                + (s.getCurrentTurn() == 'X' ? ("Ｘ\n" + s.getPlayerX()) : ("Ｏ\n" + s.getPlayerO()));
    }

    // 文本形式的棋盘
    private String printBoard(char[][] b) {
        StringBuilder sb = new StringBuilder();
        sb.append("[井字棋]\n\n");

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
