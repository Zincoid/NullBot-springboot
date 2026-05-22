package com.zincoid.nullbot.bot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.core.component.chat.current.client.QQAiClient;
import com.zincoid.nullbot.core.component.chat.current.message.QQMessage;
import com.zincoid.nullbot.core.model.data.po.SettingPO;
import com.zincoid.nullbot.core.util.BotCtxUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Chat2"})
@Component
@RequiredArgsConstructor
@Slf4j
public class Chat2Command implements Command {

    @Value("${nullbot.command.prefix}")
    private String commandPrefix;
    private final QQAiClient qqAiClient;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        SettingPO setting = BotCtxUtil.getSetting();
        try {
            qqAiClient.chat(
                    setting.getChatScope(), event.getGroupId(),
                    """
                            你是一只猫娘，名字叫Null，你在一个聊天软件中回复消息。
                            你可以通过{}在指令中嵌入命令或分割多条消息，例如：
                            1. 这是第一条{}这是第二条
                            2. 发送二次元图片 -> {Anime}
                            注意：目前在测试中""",
                    QQMessage.user(String.join(" ", params))
                            .gc(
                                    event.getGroupId(),
                                    event.getUserId(),
                                    event.getSender().getNickname()
                            ),
                    true, false, event, false
            );
        } catch (Exception e) {
            throw new NullBotMsgException("[AI] ❌出错: " + e.getMessage());
        }

        for (ArrayMsg msg : event.getArrayMsg()) {
            if (msg.getType() != MsgTypeEnum.text) continue;
            String text = msg.getData().get("text").asString().trim();
            if (text.startsWith(commandPrefix) && !text.startsWith(commandPrefix + "Chat") && !text.startsWith(commandPrefix + "对话")) {
                bot.sendGroupMsg(event.getGroupId(), """
                                [AI] ⚠️检测到指令前缀
                                - 使用指令时请不要@Null
                                - @Null仅触发AI对话
                                - Null仅可执行部分指令""",
                        false
                );
                break;
            }
        }

        log.info("\t\t\t\t├─[Chat] 群聊已回复");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Chat 命令
                功能: 与AI对话
                限权: %d 级
                格式: Chat [内容] 或 @Null [内容] 或 戳一戳
                别名: 对话""", getAccess()
        );
    }
}
