package com.zincoid.nullbot.bot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.service.basic.GroupService;
import com.zincoid.nullbot.core.service.basic.UserService;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"AccessSet", "限权设置"})
@Component
@RequiredArgsConstructor
public class AccessSetCommand implements Command {

    private final GroupService groupService;
    private final UserService userService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        String option = args.nextString();
        long targetId = args.nextLong();
        int targetNewAccess = args.nextInt();
        switch (option) {
            case "-group" -> {
                if (!groupService.exist(targetId)) throw new BotInfoException(Emoji.WARN, "群聊未注册");
                int targetAccess = groupService.getAccess(targetId);
                int selfAccess = userService.getAccess(event.getUserId());
                if (selfAccess < 2) {
                    bot.sendGroupMsg(event.getGroupId(), """
                            🚫限权操作被阻止
                            - 修改群限权需限权II
                            - 你的限权等级: %s""".formatted(selfAccess), false);
                    log.info("☑ [AccessSet] 群限权操作被阻止");
                } else {
                    groupService.setAccess(targetId, targetNewAccess);
                    bot.sendGroupMsg(event.getGroupId(), """
                            ✅群限权已修改
                            - 群聊: %s
                            - 变动: %s -> %s""".formatted(targetId, targetAccess, targetNewAccess), false);
                    log.info("☑ [AccessSet] 群限权已修改 - {} -> {}", targetId, targetNewAccess);
                }
            }
            case "-user" -> {
                if (!userService.exist(targetId)) throw new BotInfoException(Emoji.WARN, "用户未注册");
                int targetAccess = userService.getAccess(targetId);
                int selfAccess = userService.getAccess(event.getUserId());
                if (targetAccess >= selfAccess) {
                    bot.sendGroupMsg(event.getGroupId(), """
                            🚫限权操作被阻止
                            - 限权未超过目标
                            - 目标限权等级: %s
                            - 你的限权等级: %s""".formatted(targetAccess, selfAccess), false);
                    log.info("☑ [AccessSet] 用户限权操作被阻止");
                } else if (targetNewAccess >= selfAccess) {
                    bot.sendGroupMsg(event.getGroupId(), """
                            🚫限权操作被阻止
                            - 限权未超过新限权
                            - 新的限权等级: %s
                            - 你的限权等级: %s""".formatted(targetNewAccess, selfAccess), false);
                    log.info("☑ [AccessSet] 用户限权操作被阻止");
                } else {
                    userService.setAccess(targetId, targetNewAccess);
                    bot.sendGroupMsg(event.getGroupId(), """
                            ✅用户限权已修改
                            - 用户: %s
                            - 变动: %s -> %s""".formatted(targetId, targetAccess, targetNewAccess), false);
                    log.info("☑ [AccessSet] 用户限权已修改 - {} -> {}", targetId, targetNewAccess);
                }
            }
            default -> throw new BotWarnException("无此操作");
        }
    }

    @Override
    public Integer getAccess() { return -1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ AccessSet 命令
                功能: 设置用户/群组限权等级
                限权: %d 级
                格式: AccessSet [-user|-group] [ID] [限权等级]
                别名: 限权设置
                注意:
                针对用户 - 无法修改高于或等于自身限权用户, 设置的限权无法高于或等于自身限权
                针对群组 - 仅限权等级2用户可修改, 群组限权[-2]将拒绝包括此项的所有命令""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ AccessSet 命令
                功能: 设置用户/群组限权等级
                格式: AccessSet [-user|-group] [ID] [限权等级]
                示例: AccessSet -user 2660181154 2""";
    }
}
