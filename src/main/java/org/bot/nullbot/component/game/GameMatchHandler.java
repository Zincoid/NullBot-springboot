package org.bot.nullbot.component.game;

import org.bot.nullbot.entity.game.basic.Match;
import org.bot.nullbot.entity.game.basic.Player;

public interface GameMatchHandler
{
    String gameType();

    // 判断是否能够匹配
    boolean canMatch(Player p1, Player p2);

    // 游戏开始前初始化
    void onMatchStart(Match match);

    // 游戏结束后的清理
    void onMatchEnd(Match match);
}
