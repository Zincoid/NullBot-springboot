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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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

    private final Set<Long> inGameUsers = ConcurrentHashMap.newKeySet();
    private static final int QUESTION_TIMEOUT = 60;  // Second

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long groupId = groupMessageEvent.getGroupId();
            Long userId = groupMessageEvent.getUserId();
            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
            List<String> params = event.getCommandParameters();

            if (inGameUsers.contains(userId))
                throw new NullBotMsgException("[问答] ⚠️已在游戏中");

            try {
                inGameUsers.add(userId);
                bot.sendGroupMsg(groupId, "⏳问题生成中, 请稍候...", false);
                String raw;
                try {
                    raw = deepSeekClient.chatSingle("""
                                    出一道单选题并给出题目和答案
                                    问题主题:%s,生成种子:%s
                                    (注:将答案用{}包围放在开头,例如{正确选项字母},无需答案解析,选项要换行)
                                    (注:禁止生成中国国内政治事件和政治人物相关的问题,此时仅回复REFUSED)"""
                                    .formatted(params.isEmpty() ? "二次元" : String.join(" ", params), UUID.randomUUID()),
                            true, 2500
                    );
                    // log.info("[Question] generated: {}", raw);  // DEBUG
                } catch (Exception e) {
                    throw new NullBotMsgException("""
                            [问答] ❌生成请求出错
                            - User: [CQ:at,qq=%s]""".formatted(userId)
                    );
                }

                if (raw.contains("REFUSED"))
                    throw new NullBotMsgException("""
                            [问答] ❌生成问题敏感
                            - User: [CQ:at,qq=%s]""".formatted(userId)
                    );

                Pattern answerPattern = Pattern.compile("\\{([A-Za-z])}");
                Matcher answerMatcher = answerPattern.matcher(raw);

                if (!answerMatcher.find())
                    throw new NullBotMsgException("""
                            [问答] ❌生成格式异常
                            - User: [CQ:at,qq=%s]""".formatted(userId)
                    );

                String answer = answerMatcher.group(1).toUpperCase();
                String question = """
                            请[CQ:at,qq=%s]回答问题！
                            %s
                            注: 请直接发送选项, 限时%s秒！"""
                        .formatted(userId, raw.replaceFirst("\\{[A-Za-z]}\\s*", ""), QUESTION_TIMEOUT);

                bot.sendGroupMsg(groupId, question, false);
                String next = botNextInputer.request(userId, QUESTION_TIMEOUT);

                String response;
                if (next == null)
                    response = "%s回答超时！答案是...%s！".formatted(userName, answer);
                else if (answer.equals(next.toUpperCase()))
                    response = "%s回答正确！".formatted(userName);
                else
                    response = "%s回答错误！答案是...%s！".formatted(userName, answer);

                bot.sendGroupMsg(groupId, response, false);
            } finally {
                inGameUsers.remove(userId);
            }
        } else
            throw new NullBotLogException("[问答] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Question 命令
                功能: 问答题 (默认二次元主题)
                限权: %d 级
                格式: Question [可选: 主题]
                别名: 问答""", getAccess()
        );
    }
}
