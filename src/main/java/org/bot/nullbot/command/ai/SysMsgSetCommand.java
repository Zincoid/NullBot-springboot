package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.component.control.SettingManager;
import org.bot.nullbot.component.storage.SysMsgStorage;
import org.bot.nullbot.entity.CommandEvent;
import org.springframework.stereotype.Component;

@CommandMapping({"SysMsgSet", "自定义提示词"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SysMsgSetCommand implements Command
{
    private final DeepSeekClient deepSeekClient;
    private final SysMsgStorage sysMsgStorage;
    private final SettingManager settingManager;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long groupId = groupMessageEvent.getGroupId();
            if (!settingManager.getChatOption(groupId).isCustom()) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[自定义提示词] ❌非Custom模式", false);
                log.info("\t\t\t\t├─[AI.SysMsgSet] 非Custom模式");
                return;
            }
            if(!event.getCommandParameters().isEmpty()){
                Long userId = groupMessageEvent.getSender().getUserId();
                String systemMessage = String.join(" ", event.getCommandParameters());

                deepSeekClient.clearHistory(groupId, userId, settingManager.getChatOption(groupId));
                sysMsgStorage.setCustomMessage(groupId, systemMessage);

                bot.sendGroupMsg(groupId, "[自定义提示词] ✅已设置！", false);
                log.info("\t\t\t\t├─[AI.SysMsgSet] 自定义系统消息已设置 - {}", systemMessage);
            }else{
                bot.sendGroupMsg(groupId, "[自定义提示词] ❌无参数", false);
                log.info("\t\t\t\t├─[AI.SysMsgSet] 无参数");
            }
        }else
            log.info("\t\t\t\t├─[AI.SysMsgSet] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ SysMsgSet 命令
                功能: 设置AI自定义消息模式下的系统提示词(并清空历史)
                限权: %d 级
                格式: SysMsgSet [提示词]
                中文命令: 自定义提示词""", getAccess()
        );
    }
}
