package org.bot.nullbot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.control.SettingManager;
import org.bot.nullbot.entity.CommandEvent;
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
            if (params.size() < 2){
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[群设置] ❌参数不足", false);
                log.info("\t\t\t\t├─[GroupSet] 参数不足");
                return;
            }

            Long groupId = groupMessageEvent.getGroupId();
            String option = params.get(0);

            if ("-monitor".equals(option)) {
                String setting = params.get(1);
                boolean isEnable = switch (setting) {
                    case "imgCollect" -> settingManager.switchImageCollect(groupId);
                    case "msgCollect" -> settingManager.switchMessageCollect(groupId);
                    case "keywordDetect" -> settingManager.switchKeywordDetect(groupId);
                    case "pokeDetect" -> settingManager.switchPokeDetect(groupId);
                    case "recallDetect" -> settingManager.switchRecallDetect(groupId);
                    default -> {
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[功能控制] ❌无此监听设置", false);
                        log.info("\t\t\t\t├─[GroupSet] 无此监听设置 - {}", setting);
                        throw new NoSuchMethodException("" + method);
                    }
                };
            }

            if (isEnabled != null){
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[功能控制] \uD83D\uDD04状态已切换: " + (isEnabled ? "ON" : "OFF"), false);
                log.info("\t\t\t\t├─[GroupSet] 已更改群设置 {} -> {}", function, isEnabled ? "ON" : "OFF");
            }else{

            }
        }else
            log.info("\t\t\t\t├─[GroupSet] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ FuncCtrl 命令
                功能: 转换功能启用状态(全局)
                限权: %d 级
                格式: FuncCtrl [功能控制标志]
                标志: imageCollect/keywordDetect/pokeDetect/messageCollect/recallDetect
                中文命令: 功能控制""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ FuncCtrl 命令
                功能: 转换功能启用状态
                限权: %d 级
                格式: FuncCtrl [功能控制标志]
                标志: imageCollect/keywordDetect/pokeDetect/messageCollect/recallDetect
                注意: 只有Zincoid可以调用！！！""", getAccess()
        );
    }
}
