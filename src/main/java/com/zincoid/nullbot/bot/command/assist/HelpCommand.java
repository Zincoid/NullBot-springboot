package com.zincoid.nullbot.bot.command.assist;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.resource.ResourceLoader;
import com.zincoid.nullbot.core.util.Base64Util;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"Help", "help", "帮助"})
@Component
@RequiredArgsConstructor
public class HelpCommand implements Command {

    private final ResourceLoader resourceLoader;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        bot.sendGroupMsg(event.getGroupId(), buildHelpMsg(), false);
        log.info("☑ [Help] 群聊帮助已获取");
    }

    @Override
    public void execute(Bot bot, PrivateMessageEvent event, CommandArgs args) throws Exception {
        bot.sendPrivateMsg(event.getUserId(), """
                [ ====== 可用指令 ====== ]
                1. Help 帮助
                2. SysMsgSet 提示词设置
                - 参数: [-set|-reset] [文本]
                
                注: 私聊仅实现AI聊天及以上指令且永久处于无鉴权限速/非语音防注入/EMBEDDING对话策略下""", true);
        log.info("☑ [Help] 私聊帮助已获取");
    }

    private String buildHelpMsg() {
        String helpPath = resourceLoader
                .getCache("static/help/help.jpg").toAbsolutePath().toString();
        return MsgUtils.builder().img("base64://" + Base64Util.from(helpPath)).build();
    }

    @Override
    public Integer getAccess() { return -1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Help 命令
                功能: 发送帮助菜单
                限权: %d 级
                格式: Help
                别名: help/帮助""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ Help 命令
                功能: 发送帮助菜单
                格式: Help
                注意: 当有人想要了解你的功能时使用该指令""";
    }
}
