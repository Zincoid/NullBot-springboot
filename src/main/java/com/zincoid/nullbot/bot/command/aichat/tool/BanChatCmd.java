package com.zincoid.nullbot.bot.command.aichat.tool;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.gateway.handler.AuthHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.aichat.ChatCmd;
import com.zincoid.nullbot.bot.command.aichat.PokeReactCmd;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"1e7bd161"})
@Component
@RequiredArgsConstructor
public class BanChatCmd implements Cmd {

    private final AuthHandler authHandler;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        banChat(bot, event.getGroupId(), event.getUserId(), args.nextInt());
    }
    @Override
    public void run(Bot bot, PokeNoticeEvent event, CmdArgs args) {
        banChat(bot, event.getGroupId(), event.getUserId(), args.nextInt());
    }

    private void banChat(Bot bot, Long groupId, Long userId, int banTime) {
        authHandler.setUserBan(userId, ChatCmd.class, banTime);
        authHandler.setUserBan(userId, PokeReactCmd.class, banTime);
        if (banTime > 0) {
            bot.sendGroupMsg(groupId, "⛔️已封禁[CQ:at,qq=%s](%s Min)".formatted(userId, banTime), false);
            log.info("☑ [BanChat] 对话已封禁 - {} -> {} Min", userId, banTime);
        } else {
            bot.sendGroupMsg(groupId, "✅已解封[CQ:at,qq=%s]".formatted(userId), false);
            log.info("☑ [BanChat] 对话已解封 - {} -> Unblock", userId);
        }
    }

    @Override
    public Integer getAccess() { return 2; }

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
