package org.bot.nullbot.component.game;

import org.bot.nullbot.entity.game.basic.Match;
import org.bot.nullbot.entity.game.basic.Player;

public abstract class GameMatchHandler
{
    // 每种游戏的匹配方式可能不一样
    public abstract boolean canMatch(Player p1, Player p2);

    // 游戏开始前可做初始化
    public abstract void onMatchStart(Match match);

    // 游戏结束清理
    public abstract void onMatchEnd(Match match);
}
