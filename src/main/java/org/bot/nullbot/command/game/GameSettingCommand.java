package org.bot.nullbot.command.game;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.storage.GuessStorage;
import org.bot.nullbot.entity.CommandEvent;
import org.springframework.stereotype.Component;

@CommandMapping({"GameSetting", "游戏设置", "设置"})
@Component
@Slf4j
@RequiredArgsConstructor
public class GameSettingCommand implements Command
{
    private final GuessStorage guessStorage;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long groupId = groupMessageEvent.getGroupId();
            if (event.getCommandParameters().isEmpty()){
                bot.sendGroupMsg(groupId, "[游戏设置] ❌参数不足", false);
            }
            String gameType = event.getCommandParameters().getFirst();
            if("Guess".equals(gameType)){
                if(event.getCommandParameters().size() < 3){
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[游戏设置] ❌Guess游戏参数不足", false);
                    log.info("\t\t\t\t├─[GameSetting] Guess游戏参数不足");
                }else{
                    try {
                        double ratio = Double.parseDouble(event.getCommandParameters().get(1));
                        int padding = Integer.parseInt(event.getCommandParameters().get(2));
                        guessStorage.setRatio(ratio);
                        guessStorage.setPadding(padding);
                        bot.sendGroupMsg(groupId, "[游戏设置] ✅参数已更新", false);
                        log.info("\t\t\t\t├─[GameSetting] 已更新 - {}", gameType);
                    } catch (NumberFormatException e) {
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[游戏设置] ❌Guess游戏参数格式错误", false);
                        log.info("\t\t\t\t├─[GameSetting] Guess游戏参数格式错误");
                    }
                }
            }else{
                bot.sendGroupMsg(groupId, "[游戏设置] ❌该游戏不存在", false);
                log.info("\t\t\t\t├─[GameSetting] 该游戏不存在 - {}", gameType);
            }
        }else
            log.info("\t\t\t\t├─[GameSetting] 未设计 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() {
        return 1;
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ GameSetting 命令
                功能: 游戏参数设置
                限权: %d
                格式: GameSetting [游戏类型] [参数...]
                游戏参数:
                Guess(猜角色) - [Ratio] [Padding]
                中文命令: 游戏设置/设置""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ GameSetting 命令
                功能: 设置游戏参数
                限权: %d
                格式: GameSetting [游戏类型] [参数...]
                游戏参数:
                Guess(猜角色) - [Ratio(范围 0.05-0.3)] [Padding(范围 150-300)]
                示例: GameSetting Guess 0.15 300
                注意: 针对Guess游戏 - Ratio越小越难 Padding越小越难""", getAccess()
        );
    }
}
