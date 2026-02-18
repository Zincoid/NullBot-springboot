package org.bot.nullbot.command.game.single;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.component.control.BotNextInputer;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandMapping({"Question", "问答"})
@Component
@Slf4j
@RequiredArgsConstructor
public class QuestionCommand implements Command
{
    private final DeepSeekClient deepSeekClient;
    private final BotNextInputer botNextInputer;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long groupId = groupMessageEvent.getGroupId();
            Long userId = groupMessageEvent.getUserId();

            String response;
            try {
                response = deepSeekClient.chatSingle("出一道二次元选择题并给出答案，将答案用{}包围放在开头，例如{A}");
            } catch (Exception e) {
                throw new NullBotMsgException("[问答] ❌生成问题出错");
            }

            Pattern answerPattern = Pattern.compile("\\{([A-Z])}");
            Matcher answerMatcher = answerPattern.matcher(response);

            if (answerMatcher.find()) {
                String answer = answerMatcher.group(1);
                String question = response.replaceFirst("\\{[A-Z]}\\s*", "");
                bot.sendGroupMsg(groupId, question + "\n注: 请直接回复选项！", false);
                String next = botNextInputer.request(userId, 30);
                if (next == null) {
                    bot.sendGroupMsg(groupId, "超时啦！答案是...%s！".formatted(answer), false);
                    return;
                }
                if (answer.equals(next))
                    bot.sendGroupMsg(groupId, "回答正确！", false);
                else
                    bot.sendGroupMsg(groupId, "回答错误！答案是...%s！".formatted(answer), false);
            } else
                throw new NullBotMsgException("[问答] ❌生成异常问题");
        }else
            throw new NullBotLogException("[问答] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Question 命令
                功能: 二次元问答题
                限权: %d 级
                格式: Question
                别名: 问答""", getAccess()
        );
    }
}
