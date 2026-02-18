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

import java.util.List;
import java.util.UUID;
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

    private static final int QUESTION_TIMEOUT = 30;  // Second

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long groupId = groupMessageEvent.getGroupId();
            Long userId = groupMessageEvent.getUserId();
            List<String> params = event.getCommandParameters();

            bot.sendGroupMsg(groupId, "⏳问题生成中, 请稍候...", false);
            String raw;
            try {
                raw = deepSeekClient.chatSingle(
                        "出一道单选题并给出题目和答案,问题主题:%s,生成种子:%s (注:将答案用{}包围放在开头,例如{正确选项},无需答案解析)"
                        .formatted(params.isEmpty() ? "二次元" : params.getFirst(), UUID.randomUUID()),
                        true, 1000
                );
            } catch (Exception e) {
                throw new NullBotMsgException("[问答] ❌生成请求出错");
            }

            Pattern answerPattern = Pattern.compile("\\{([A-Za-z])}");
            Matcher answerMatcher = answerPattern.matcher(raw);

            if (answerMatcher.find()) {
                String answer = answerMatcher.group(1).toUpperCase();
                String question = """
                        %s
                        注: 请直接回复选项！限时%s秒！"""
                        .formatted(raw.replaceFirst("\\{[A-Za-z]}\\s*", ""), QUESTION_TIMEOUT);
                String response;

                bot.sendGroupMsg(groupId, question, false);
                String next = botNextInputer.request(userId, QUESTION_TIMEOUT);

                if (next == null)
                    response = "超时啦！答案是...%s！".formatted(answer);
                else if (answer.equals(next.toUpperCase()))
                    response = "回答正确！";
                else
                    response = "回答错误！答案是...%s！".formatted(answer);

                bot.sendGroupMsg(groupId, response, false);
            } else
                throw new NullBotMsgException("[问答] ❌生成问题异常");
        }else
            throw new NullBotLogException("[问答] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Question 命令
                功能: 二次元问答题
                限权: %d 级
                格式: Question [可选: 主题]
                别名: 问答""", getAccess()
        );
    }
}
