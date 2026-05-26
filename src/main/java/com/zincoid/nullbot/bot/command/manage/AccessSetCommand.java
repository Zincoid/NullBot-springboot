package com.zincoid.nullbot.bot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.service.GroupService;
import com.zincoid.nullbot.core.service.UserService;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"AccessSet", "限权设置"})
@Component
@RequiredArgsConstructor
@Slf4j
public class AccessSetCommand implements Command {

    private final GroupService groupService;
    private final UserService userService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        if (params.size() < 3)
            throw new NullBotException("[限权设置] ❌参数不足");

        long targetId;
        int targetNewAccess;
        try {
            targetId = Long.parseLong(params.get(1));
            targetNewAccess = Integer.parseInt(params.get(2));
        } catch (NumberFormatException e) {
            throw new NullBotException("[限权设置] ❌参数格式错误");
        }

        switch (params.get(0))
        {
            case "-GROUP" -> {
                if (!groupService.exist(targetId)) throw new NullBotException("[限权设置] ❌群聊未注册");
                int targetAccess = groupService.getAccess(targetId);
                int selfAccess = userService.getAccess(event.getUserId());
                if (selfAccess < 2) {
                    bot.sendGroupMsg(event.getGroupId(), """
                            [限权设置] \uD83D\uDEAB修改失败
                            - 仅限权等级II用户可修改群限权
                            - 你的限权等级: %s""".formatted(selfAccess), false);
                    log.info("├─[AccessSet] 修改失败 - 仅限权等级2用户可修改群限权 用户限权为{}", selfAccess);
                } else {
                    groupService.setAccess(targetId, targetNewAccess);
                    bot.sendGroupMsg(event.getGroupId(), """
                            [限权设置] ✅群限权已修改
                            - 变动群聊: %s
                            - 变动详情: %s -> %s""".formatted(targetId, targetAccess, targetNewAccess), false);
                    log.info("├─[AccessSet] 已修改群聊 {} 限权 - {} -> {}", targetId, targetAccess, targetNewAccess);
                }
            }

            case "-USER" -> {
                if (!userService.exist(targetId)) throw new NullBotException("[限权设置] ❌用户未注册");
                int targetAccess = userService.getAccess(targetId);
                int selfAccess = userService.getAccess(event.getUserId());
                if (targetAccess >= selfAccess) {
                    bot.sendGroupMsg(event.getGroupId(), """
                            [限权设置] \uD83D\uDEAB修改失败
                            - 目标限权高于或等于你的限权
                            - 目标限权等级: %s
                            - 你的限权等级: %s""".formatted(targetAccess, selfAccess), false);
                    log.info("├─[AccessSet] 修改失败 - 目标限权等级{} 高于或等于 自身限权等级{}", targetAccess, selfAccess);
                } else if(targetNewAccess >= selfAccess) {
                    bot.sendGroupMsg(event.getGroupId(), """
                            [限权设置] \uD83D\uDEAB修改失败
                            - 新的限权高于或等于你的限权
                            - 新的限权等级: %s
                            - 你的限权等级: %s""".formatted(targetNewAccess, selfAccess), false);
                    log.info("├─[AccessSet] 修改失败 - 新限权等级{} 高于或等于 自身限权等级{}", targetNewAccess, selfAccess);
                } else {
                    userService.setAccess(targetId, targetNewAccess);
                    bot.sendGroupMsg(event.getGroupId(), """
                            [限权设置] ✅用户限权已修改
                            - 变动用户: %s
                            - 变动详情: %s -> %s""".formatted(targetId, targetAccess, targetNewAccess), false);
                    log.info("├─[AccessSet] 已修改用户 {} 限权 - {} -> {}", targetId, targetAccess, targetNewAccess);
                }
            }

            default -> throw new NullBotException("[限权设置] ❌修改选项不存在");
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
                格式: AccessSet [-USER|-GROUP] [ID] [限权等级]
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
                格式: AccessSet [-USER|-GROUP] [ID] [限权等级]
                示例: AccessSet -USER 2660181154 2""";
    }
}
