package org.bot.nullbot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.GroupService;
import org.bot.nullbot.service.UserService;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"AccessSet", "限权设置"})
@Component
@RequiredArgsConstructor
@Slf4j
public class AccessSetCommand implements Command
{
    private final GroupService groupService;
    private final UserService userService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            if (params.size() < 3)
                throw new NullBotMsgException("[限权设置] ❌参数不足");

            long targetId;
            int targetNewAccess;
            try {
                targetId = Long.parseLong(params.get(1));
                targetNewAccess = Integer.parseInt(params.get(2));
            } catch (NumberFormatException e) {
                throw new NullBotMsgException("[限权设置] ❌参数格式错误");
            }

            switch (params.get(0))
            {
                case "-GROUP" -> {
                    if(!groupService.existGroup(targetId)) throw new NullBotMsgException("[限权设置] ❌群聊未注册");
                    int targetAccess = groupService.getGroupAccess(targetId);
                    int selfAccess = userService.getUserAccess(groupMessageEvent.getUserId());
                    if(selfAccess < 2){
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[限权设置] \uD83D\uDEAB修改失败\n仅限权等级II用户可修改群限权\n你的限权等级: " + selfAccess, false);
                        log.info("\t\t\t\t├─[AccessSet] 修改失败 - 仅限权等级2用户可修改群限权 用户限权为{}", selfAccess);
                    }else{
                        groupService.setGroupAccess(targetId, targetNewAccess);
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[限权设置] ✅已修改群聊 " + targetId + " 限权等级:\n" + targetAccess + " -> " + targetNewAccess, false);
                        log.info("\t\t\t\t├─[AccessSet] 已修改群聊 {} 限权 - {} -> {}", targetId, targetAccess, targetNewAccess);
                    }
                }

                case "-USER" -> {
                    if(!userService.existUser(targetId)) throw new NullBotMsgException("[限权设置] ❌用户未注册");
                    int targetAccess = userService.getUserAccess(targetId);
                    int selfAccess = userService.getUserAccess(groupMessageEvent.getUserId());
                    if(targetAccess >= selfAccess){
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[限权设置] \uD83D\uDEAB修改失败\n目标限权等级: " + targetAccess + "\n高于或等于\n你的限权等级: " + selfAccess, false);
                        log.info("\t\t\t\t├─[AccessSet] 修改失败 - 目标限权等级{} 高于或等于 自身限权等级{}", targetAccess, selfAccess);
                    }else if(targetNewAccess >= selfAccess){
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[限权设置] \uD83D\uDEAB修改失败\n新的限权等级: " + targetNewAccess + "\n高于或等于\n你的限权等级: " + selfAccess, false);
                        log.info("\t\t\t\t├─[AccessSet] 修改失败 - 新限权等级{} 高于或等于 自身限权等级{}", targetNewAccess, selfAccess);
                    }else{
                        userService.setUserAccess(targetId, targetNewAccess);
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[限权设置] ✅已修改用户 " + targetId + " 限权等级:\n" + targetAccess + " -> " + targetNewAccess, false);
                        log.info("\t\t\t\t├─[AccessSet] 已修改用户 {} 限权 - {} -> {}", targetId, targetAccess, targetNewAccess);
                    }
                }

                default -> throw new NullBotMsgException("[限权设置] ❌修改选项不存在");
            }
        }else
            throw new NullBotLogException("[限权设置] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return -1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ AccessSet 命令
                功能: 设置用户/群组限权等级
                限权: %d 级
                格式: AccessSet [-USER|-GROUP] [ID] [限权等级]
                中文命令: 限权设置
                注意:
                针对用户 - 无法修改高于或等于自身限权用户, 设置的限权无法高于或等于自身限权
                针对群组 - 仅限权等级2用户可修改, 群组限权[-2]将拒绝包括此项的所有命令""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ AccessSet 命令
                功能: 设置用户/群组限权等级
                限权: %d 级
                格式: AccessSet [-USER|-GROUP] [ID] [限权等级]
                示例: AccessSet -USER 2660181154 2""", getAccess()
        );
    }
}
