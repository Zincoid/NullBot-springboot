package org.bot.nullbot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.control.SettingManager;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.info.SettingInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"GroupSet", "群设置"})
@Component
@RequiredArgsConstructor
@Slf4j
public class GroupSetCommand implements Command
{
    private final SettingManager settingManager;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            Long groupId = groupMessageEvent.getGroupId();
            try {
                if (params.isEmpty()) throw new IllegalArgumentException("参数不足");
                String option = params.get(0);

                if ("-view".equals(option)) {
                    SettingInfo setting = settingManager.getSetting(groupId);
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[群设置] ℹ️已获取！\n" + setting, false);
                    log.info("\t\t\t\t├─[GroupSet] 已获取群设置 - {}", groupId);
                    return;
                }
                if ("-monitor".equals(option)) {
                    String setting = params.get(1);
                    boolean isEnabled = switch (setting) {
                        case "img" -> settingManager.switchImageCollect(groupId);
                        case "msg" -> settingManager.switchMessageCollect(groupId);
                        case "key" -> settingManager.switchKeywordDetect(groupId);
                        case "pok" -> settingManager.switchPokeDetect(groupId);
                        case "rcl" -> settingManager.switchRecallDetect(groupId);
                        default -> throw new NoSuchMethodException("无此监听设置");
                    };
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[群设置] \uD83D\uDD04状态已切换: " + (isEnabled ? "ON" : "OFF"), false);
                    log.info("\t\t\t\t├─[GroupSet] 已更改群设置 {} -> {}", setting, isEnabled ? "ON" : "OFF");
                    return;
                }

                throw new NoSuchMethodException("无此设置");
            } catch (Exception e) {
                bot.sendGroupMsg(groupId, "[群设置] ❌" + e.getMessage(), false);
                log.info("\t\t\t\t├─[GroupSet] 群设置出错 - {}", e.getMessage());
            }
        }else
            log.info("\t\t\t\t├─[GroupSet] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ GroupSet 命令
                功能: 设置群功能
                限权: %d 级
                格式: GroupSet [类型] [可选: 参数...]
                类型和参数:
                -view (获取群设置)
                -monitor [img(图片收集)|msg(消息收集)|key(关键词检测)|pok(戳一戳检测)|rcl(撤回检测)]
                中文命令: 群设置""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ GroupSet 命令
                功能: 设置群功能
                限权: %d 级
                格式: GroupSet [类型] [可选: 参数...]
                类型和参数:
                -view (获取群设置)
                -monitor [img(图片收集)|msg(消息收集)|key(关键词检测)|pok(戳一戳检测)|rcl(撤回检测)]
                示例: GroupSet -monitor img
                注意: 只有Zincoid可以调用！！！""", getAccess()
        );
    }
}
