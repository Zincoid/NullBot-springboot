package org.bot.nullbot.command.game.looting;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.game.handler.LootingMatchHandler;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.result.GameResult;
import org.springframework.stereotype.Component;


@CommandMapping({"Looting", "摸金"})
@Component
@RequiredArgsConstructor
@Slf4j
public class LootingCommand implements Command
{
    private final LootingMatchHandler lootingMatchHandler;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if ((event.getEvent() instanceof GroupMessageEvent groupMessageEvent)) {
            Long userId = groupMessageEvent.getUserId();
            String commandText = event.getCommandParameters().isEmpty() ? "侦察" : String.join(" ", event.getCommandParameters());
            GameResult result = lootingMatchHandler.action(userId, commandText);

            if(result.getSuccess()){
                if(result.getIsAsync()){
                    if(!result.getSelfInfo().isEmpty())
                        bot.sendGroupMsg(result.getSelfGroupId(), result.getSelfInfo(), false);
                    if(!result.getOpponentInfo().isEmpty())
                        bot.sendGroupMsg(result.getOpponentGroupId(), result.getOpponentInfo(), false);
                }else
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[摸金] ❌该模式不发送同步消息", false);
            }else
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), result.getSelfInfo(), false);

            log.info("\t\t\t\t├─[Looting] 玩家 {} 执行指令 [{}]", userId, commandText);
        }else{
            log.info("\t\t\t\t├─[Looting] 未设计 非群消息事件响应方式");
        }
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Looting 命令
                功能: 双人 PVPVE 非回合制摸金对抗
                限权: %s
                中文指令: 摸金
                
                基础指令:
                - Looting 侦察
                - Looting 移动 [地点]
                - Looting 搜刮
                - Looting 攻击AI
                - Looting 攻击玩家
                - Looting 撤离
                
                说明:
                - 任意玩家行动(侦察除外)都会推进游戏刻
                - AI 会在 Tick 中移动 / 攻击
                - 25 Tick后未撤离则迷失并掉落全部物品""", getAccess()
        );
    }
}

