package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.component.storage.SysMsgStorage;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.SettingService;
import org.bot.nullbot.service.UserService;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"SysMsgSet", "提示词设置"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SysMsgSetCommand implements Command
{
    private final DeepSeekClient deepSeekClient;
    private final SysMsgStorage sysMsgStorage;
    private final SettingService settingService;
    private final UserService userService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            Long groupId = groupMessageEvent.getGroupId();
            Long userId = groupMessageEvent.getSender().getUserId();
            String option = params.getFirst();

            if(params.isEmpty()) throw new NullBotMsgException("[提示词设置] ❌参数不足");
            if ("-reset".equals(option)) {
                if (userService.getUserAccess(userId) < 1)
                    throw new NullBotMsgException("[提示词设置] \uD83D\uDEAB重置失败\n仅限权等级I及以上用户可重置提示词");
                deepSeekClient.clearHistory(groupId, userId, settingService.getChatOption(groupId));
                sysMsgStorage.reset(groupId);
                bot.sendGroupMsg(groupId, "[提示词设置] ✅已重置！", false);
                log.info("\t\t\t\t├─[SysMsgSet] 提示词已重置 - {}", groupId);
                return;
            }

            if(params.size() < 2) throw new NullBotMsgException("[提示词设置] ❌参数不足");
            if ("-default".equals(option)) {
                if (settingService.getChatOption(groupId).isCustom())
                    throw new NullBotMsgException("[提示词设置] ❌非Default模式");
                if (userService.getUserAccess(userId) < 1)
                    throw new NullBotMsgException("[提示词设置] \uD83D\uDEAB设置失败\n仅限权等级I及以上用户可修改Default提示词");
                String defaultMessage = String.join(" ", params.subList(1, params.size()));
                deepSeekClient.clearHistory(groupId, userId, settingService.getChatOption(groupId));
                sysMsgStorage.setDefaultMessage(groupId, defaultMessage);
                bot.sendGroupMsg(groupId, "[提示词设置] ✅Default模式: 已设置！", false);
                log.info("\t\t\t\t├─[SysMsgSet] Default提示词已设置 - {} -> {}", groupId, defaultMessage);
                return;
            }
            if ("-custom".equals(option)) {
                if (!settingService.getChatOption(groupId).isCustom())
                    throw new NullBotMsgException("[提示词设置] ❌非Custom模式");
                String customMessage = String.join(" ", params.subList(1, params.size()));
                deepSeekClient.clearHistory(groupId, userId, settingService.getChatOption(groupId));
                sysMsgStorage.setCustomMessage(groupId, customMessage);
                bot.sendGroupMsg(groupId, "[提示词设置] ✅Custom模式: 已设置！", false);
                log.info("\t\t\t\t├─[SysMsgSet] Custom提示词已设置 - {} -> {}", groupId, customMessage);
                return;
            }

            throw new NullBotMsgException("[提示词设置] ❌无此操作");
        }else
            throw new NullBotLogException("[提示词设置] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ SysMsgSet 命令
                功能: 设置AI系统提示词并清空历史 (部分操作需二次限权验证)
                限权: %d 级
                格式: SysMsgSet [-reset|-default|-custom] [可选: 提示词]
                别名: 提示词设置
                注意:
                - 模式切换 使用 GroupSet 群设置指令
                - Reset操作 仅限权I及以上可重置全部提示词
                - Default模式 仅限权I及以上可修改提示词
                - Custom模式 仅限权0及以上可修改提示词
                - Custom模式 默认禁用指令模式""", getAccess()
        );
    }
}
