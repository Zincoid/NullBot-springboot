package com.zincoid.nullbot.bot.command.ai.embedding;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.command.ai.ChatCommand;
import com.zincoid.nullbot.bot.command.ai.PokeReactCommand;
import com.zincoid.nullbot.bot.dispatcher.handler.impl.PermissionHandler;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import com.zincoid.nullbot.core.service.UserService;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"1e7bd161"})  // 加密 仅供AI嵌入调用
@Component
@RequiredArgsConstructor
@Slf4j
public class BanChatCommand implements Command {

    private final UserService userService;
    private final PermissionHandler permissionHandler;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        banChat(bot, params, event.getGroupId());
    }

    @Override
    public void execute(Bot bot, PokeNoticeEvent event, List<String> params) {
        banChat(bot, params, event.getGroupId());
    }

    private void banChat(Bot bot, List<String> params, Long groupId) {
        if (params.size() < 2)
            throw new NullBotMsgException("[停用AI] ❌参数不足");
        long userId;
        int banTime;
        try {
            userId = Long.parseLong(params.get(0));
            banTime = Integer.parseInt(params.get(1));
        } catch (NumberFormatException e) {
            throw new NullBotMsgException("[停用AI] ❌参数格式错误");
        }
        if (!userService.exist(userId))
            throw new NullBotMsgException("[停用AI] ❌用户不存在");

        permissionHandler.setUserBan(userId, ChatCommand.class, banTime);
        permissionHandler.setUserBan(userId, PokeReactCommand.class, banTime);

        if (banTime > 0) {
            bot.sendGroupMsg(groupId, """
                    [停用AI] ⛔️已封禁
                    - 用户: [CQ:at,qq=%s]
                    - 时长: %s Min""".formatted(userId, banTime), false);
            log.info("\t\t\t\t├─[BanChat] 已封禁对话 - {} -> {} Min", userId, banTime);
        } else {
            bot.sendGroupMsg(groupId, """
                    [停用AI] ✅已解封
                    - 用户: [CQ:at,qq=%s]""".formatted(userId), false);
            log.info("\t\t\t\t├─[BanChat] 已解封对话 - {}", userId);
        }
    }

    @Override
    public Integer getAccess() { return 2; }

    // 加密命令 无用户帮助

    @Override
    public String getHelpForAI() {
        return """
                ◉ 1e7bd161 命令
                功能: 临时封禁某人与你进行对话的功能(时长为0则解除封禁)
                格式: 1e7bd161 [QQ号] [时长(分钟)]
                示例: 1e7bd161 1826403984 1
                注意: 当你对某人感到厌烦时可以调用此命令，这样他就不能在你设置的时限内与你对话了
                不要太小肚鸡肠，别人只是揉你一两下的话不要总是封禁他们，就算封禁也最好只禁1分钟""";
    }
}
