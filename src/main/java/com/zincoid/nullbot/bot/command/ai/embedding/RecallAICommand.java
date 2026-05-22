package com.zincoid.nullbot.bot.command.ai.embedding;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.core.component.ai.chat.enums.Role;
import com.zincoid.nullbot.core.component.ai.chat.memory.MsgWindowChatMemory;
import com.zincoid.nullbot.core.component.ai.chat.message.QQMessage;
import com.zincoid.nullbot.core.enums.ChatScope;
import com.zincoid.nullbot.core.model.data.po.SettingPO;
import com.zincoid.nullbot.core.util.BotCtxUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"db3fbe2b"})  // 加密 仅供AI嵌入调用
@Component
@RequiredArgsConstructor
@Slf4j
public class RecallAICommand implements Command {

    private final MsgWindowChatMemory msgWindowChatMemory;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        int n = 1;
        if (!params.isEmpty()) {
            try {
                n = Integer.parseInt(params.getFirst());
                if(n <= 0) throw new NullBotMsgException("[撤回AI消息] ❌参数非正");
            } catch (NumberFormatException e) {
                throw new NullBotMsgException("[撤回AI消息] ❌参数格式错误");
            }
        }

        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        SettingPO setting = BotCtxUtil.getSetting();
        String chatId = setting.getChatScope() + "_" +
                (setting.getChatScope() == ChatScope.Personal
                        ? userId : groupId);

        List<QQMessage> messages = msgWindowChatMemory.get(chatId)
                .stream().map(m -> (QQMessage) m).toList();
        List<QQMessage> filtered = messages.stream()
                .filter(msg -> msg != null && msg.getMessageId() != null && msg.getRole() == Role.ASSISTANT)
                .toList();
        int startIndex = Math.max(0, filtered.size() - n);
        List<QQMessage> targets = filtered.subList(startIndex, filtered.size());

        for (QQMessage target : targets) bot.deleteMsg(target.getMessageId());

        log.info("\t\t\t\t├─[RecallAI] 已撤回AI消息 -> {}条", n);
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelpForAI() {
        return """
                ◉ db3fbe2b 命令
                功能: 撤回AI发送的最近消息(仅文本消息)
                格式: db3fbe2b [可选: 条数(默认为1)]
                示例: db3fbe2b 1
                注意: 该指令使用时必须置于你此次所有回复内容之前
                已撤回的消息依然会存在于之后发给你的消息列表里
                撤回你自己的消息用这个，撤回其他人的消息不要用这个""";
    }
}
