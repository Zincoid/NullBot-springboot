package org.bot.nullbot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.service.GroupService;
import org.bot.nullbot.service.UserService;
import org.springframework.stereotype.Component;

@CommandMapping({"AccessSet", "限权设置"})
@Component
@RequiredArgsConstructor
@Slf4j
public class AccessSetCommand implements Command
{
    // private final AccessManager accessManager;
    private final GroupService groupService;
    private final UserService userService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if (event.getCommandParameters().size() >= 3){
                try {
                    String scope = event.getCommandParameters().get(0);
                    long targetId = Long.parseLong(event.getCommandParameters().get(1));
                    int targetNewAccess = Integer.parseInt(event.getCommandParameters().get(2));

                    switch (scope)
                    {
                        case "GROUP" -> {
                            if(!groupService.existGroup(targetId)){
                                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[限权设置] ❌群聊未注册", false);
                                log.info("\t\t\t\t├─[Access.Set] 群聊未注册 - {}", targetId);
                                return;
                            }
                            int targetAccess = groupService.getGroupAccess(targetId);
                            int selfAccess = userService.getUserAccess(groupMessageEvent.getUserId());
                            if(selfAccess < 2){
                                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[限权设置] ❌修改失败\n仅限权等级2用户可修改群限权 你的限权为" + selfAccess, false);
                                log.info("\t\t\t\t├─[Access.Set] 修改失败 - 仅限权等级2用户可修改群限权 用户限权为{}", selfAccess);
                            }else{
                                groupService.setGroupAccess(targetId, targetNewAccess);
                                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[限权设置] ✅已修改群 " + targetId + " 限权: " + targetAccess + " -> " + targetNewAccess, false);
                                log.info("\t\t\t\t├─[Access.Set] 已修改群 {} 限权 - {} -> {}", targetId, targetAccess, targetNewAccess);
                            }
                        }

                        case "USER" -> {
                            if(!userService.existUser(targetId)){
                                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[限权设置] ❌用户未注册", false);
                                log.info("\t\t\t\t├─[Access.Set] 用户未注册 - {}", targetId);
                                return;
                            }
                            int targetAccess = userService.getUserAccess(targetId);
                            int selfAccess = userService.getUserAccess(groupMessageEvent.getUserId());
                            if(targetAccess >= selfAccess){
                                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[限权设置] ❌修改失败\n目标限权等级" + targetAccess + " 高于或等于 自身限权等级" + selfAccess, false);
                                log.info("\t\t\t\t├─[Access.Set] 修改失败 - 目标限权等级{} 高于或等于 自身限权等级{}", targetAccess, selfAccess);
                            }else if(targetNewAccess >= selfAccess){
                                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[限权设置] ❌修改失败\n新限权等级" + targetNewAccess + " 高于或等于 自身限权等级" + selfAccess, false);
                                log.info("\t\t\t\t├─[Access.Set] 修改失败 - 新限权等级{} 高于或等于 自身限权等级{}", targetNewAccess, selfAccess);
                            }else{
                                userService.setUserAccess(targetId, targetNewAccess);
                                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[限权设置] ✅已修改用户 " + targetId + " 限权: " + targetAccess + " -> " + targetNewAccess, false);
                                log.info("\t\t\t\t├─[Access.Set] 已修改用户 {} 限权 - {} -> {}", targetId, targetAccess, targetNewAccess);
                            }
                        }

                        default -> {
                            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[限权设置] ❌修改选项不存在", false);
                            log.info("\t\t\t\t├─[Access.Set] 修改选项不存在");
                        }
                    }

                } catch (NumberFormatException e) {
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[限权设置] ❌参数格式错误", false);
                    log.info("\t\t\t\t├─[Access.Set] 参数格式错误");
                }
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[限权设置] ❌参数不足", false);
                log.info("\t\t\t\t├─[Access.Set] 参数不足");
            }
        }else
            log.info("\t\t\t\t├─[Access.Set] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() {
        return -1;
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ AccessSet 命令
                功能: 设置用户限权等级
                限权: %s
                注意:
                针对用户 - 无法修改高于或等于自身限权用户, 设置的限权无法高于或等于自身限权
                针对群组 - 仅限权等级2用户可修改, 群组限权[-2]将拒绝包括此项的所有命令
                格式: AccessSet [USER|GROUP] [ID] [限权等级]
                中文命令: 限权设置""", getAccess());
    }

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ AccessSet 命令
                功能: 设置用户限权等级
                限权: %s
                格式: AccessSet [USER|GROUP] [ID] [限权等级]
                例如: AccessSet USER 2660181154 2""", getAccess());
    }
}
