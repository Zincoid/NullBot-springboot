package org.bot.nullbot.component.game.impl;

import org.bot.nullbot.entity.game.basic.Match;
import org.bot.nullbot.entity.game.basic.Player;
import org.bot.nullbot.entity.game.tictactoe.TicTacToeState;
import org.bot.nullbot.component.game.GameMatchHandler;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TicTacToeMatchHandler implements GameMatchHandler
{
    // matchId → state
    private final Map<String, TicTacToeState> stateStore = new ConcurrentHashMap<>();

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
        // 玩家1先手
        TicTacToeState state = new TicTacToeState(match.getPlayer1().getUserId());
        stateStore.put(match.getMatchId(), state);
    }

    @Override
    public void onMatchEnd(Match match) {
        stateStore.remove(match.getMatchId());
    }

    public TicTacToeState getState(String matchId) {
        return stateStore.get(matchId);
    }
}
