package com.zincoid.nullbot.bot.command.assist;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.module.resource.loader.ResourceLoader;
import com.zincoid.nullbot.core.utils.Base64Util;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Help", "帮助"})
@Component
@RequiredArgsConstructor
public class HelpCmd implements Cmd {

    private final ResourceLoader resourceLoader;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        bot.sendGroupMsg(event.getGroupId(), buildHelpMsg(), false);
        log.info("☑ [Help] 群聊帮助已获取");
    }

    @Override
    public void run(Bot bot, PrivateMessageEvent event, CmdArgs args) throws Exception {
        bot.sendPrivateMsg(event.getUserId(), """
                [ ====== 可用指令 ====== ]
                1. Help 帮助
                2. SysMsgSet 提示词设置
                   选项:
                   -s,--set [文本]  设置提示词
                   -r,--reset      重置提示词
                   -v,--view       查看提示词
                
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
                功能: 帮助菜单
                限权: %d 级
                格式: Help
                别名: 帮助""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ Help 命令
                功能: 帮助菜单
                格式: Help
                注意: 当有人想要了解你的功能时使用该指令""";
    }
}
