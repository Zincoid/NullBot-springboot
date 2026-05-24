package com.zincoid.nullbot.bot.command.assist;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.resource.ResourceLoader;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import com.zincoid.nullbot.core.util.Base64Util;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Help", "help", "帮助"})
@Component
@Slf4j
@RequiredArgsConstructor
public class HelpCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final ResourceLoader resourceLoader;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        bot.sendGroupMsg(event.getGroupId(), buildHelpMsg(), false);
        log.info("\t\t\t\t├─[Help] 已获取群聊帮助");
    }

    @Override
    public void execute(Bot bot, PrivateMessageEvent event, List<String> params) throws Exception {
        bot.sendPrivateMsg(event.getUserId(), """
                [ ====== 可用指令 ====== ]
                1. Help  帮助
                - 参数: 无
                2. SysMsgSet  提示词设置
                - 参数: [-set|-reset] [文本]
                3. Tts  语言合成
                - 参数: [-synth] [文本]
                
                注: 私聊目前仅实现AI聊天及以上指令且AI永久处于无验证/无限速/非防注入/非语音/指令模式下""", true);
        log.info("\t\t\t\t├─[Help] 已获取私聊帮助");
    }

    private String buildHelpMsg() {
        try {
            String helpPath = resourceLoader
                    .getCached("static/help/help.jpg", fileStorageProperties.getTempPath())
                    .toAbsolutePath().toString();
            return MsgUtils.builder().img("base64://" + Base64Util.from(helpPath)).build();
        } catch (Exception e) {
            throw new NullBotMsgException("[帮助] ❌资源获取失败");
        }
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
