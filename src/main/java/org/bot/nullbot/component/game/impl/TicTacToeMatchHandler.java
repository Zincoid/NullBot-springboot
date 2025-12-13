package org.bot.nullbot.component.game.impl;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.component.game.GameMatchHandler;
import org.bot.nullbot.component.game.MatchManager;
import org.bot.nullbot.component.game.logic.TicTacToeGameLogic;
import org.bot.nullbot.entity.game.basic.GameResult;
import org.bot.nullbot.entity.game.basic.Match;
import org.bot.nullbot.entity.game.basic.Player;
import org.bot.nullbot.entity.game.tictactoe.TicTacToeGameState;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class TicTacToeMatchHandler implements GameMatchHandler {

    private final TicTacToeGameLogic gameLogic;
    private final MatchManager matchManager;

    private final Map<String, TicTacToeGameState> games = new ConcurrentHashMap<>();

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

        boolean sameGroup =
                match.getPlayer1().getGroupId()
                        .equals(match.getPlayer2().getGroupId());

        if (sameGroup) {
            return GameResult.success(null, info.toString());
        }

        Long opponentGroupId =
                userId.equals(match.getPlayer1().getUserId())
                        ? match.getPlayer2().getGroupId()
                        : match.getPlayer1().getGroupId();

        return GameResult.success(opponentGroupId, info.toString());
    }

    private String render(TicTacToeGameState s) {
        StringBuilder sb = new StringBuilder("【井字棋】\n");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                char c = s.getBoard()[i][j];
                sb.append(c == '.' ? "➕" : c == 'X' ? "❌" : "⭕");
            }
            sb.append("\n");
        }
        sb.append("当前回合：")
                .append(s.getCurrentTurn() == 'X' ? "❌ X" : "⭕ O");
        return sb.toString();
    }
}
