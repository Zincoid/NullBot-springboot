package org.bot.nullbot.command.game.single;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.component.control.BotNextInputer;
import org.bot.nullbot.dispatcher.handler.impl.PermissionHandler;
import org.bot.nullbot.enums.BniMode;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@CommandMapping({"Question", "问答"})
@Component
@Slf4j
@RequiredArgsConstructor
public class QuestionCommand implements Command {

    private final DeepSeekClient deepSeekClient;
    private final BotNextInputer botNextInputer;
    private final PermissionHandler permissionHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Set<Long> inGameUsers = new ConcurrentHashSet<>();

    private boolean thinking = true;  // 思考模式
    private static final int BLOCKING_TIME = 1;  // 封禁时间 (单位: Min)

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();

        if (inGameUsers.contains(userId))
            throw new NullBotMsgException("[问答] ⚠️已在游戏中");

        try {
            inGameUsers.add(userId);
            bot.sendGroupMsg(groupId, "⏳问题生成中, 请稍候...", false);
            String raw;
            try {
                raw = deepSeekClient.chatSingle("""
                                出一道单选题并给出题目和答案，问题主题：%s。请严格按照以下JSON格式回复，不要包含任何其他内容：
                                {"answer":"正确选项字母","timeout":回答限时秒数,"question":"题目内容(选项要换行)"}
                                注:
                                1. timeout根据题目难度设定，简单题15-30秒，中等题45-60秒，困难题90-120秒，
                                2. 公式相关内容不要使用Latex格式
                                3. 禁止生成中国国内政治事件和政治人物相关问题，当主题涉及时仅回复REFUSED"""
                                .formatted(params.isEmpty() ? "二次元" : String.join(" ", params)),
                        thinking, 2500
                );
            } catch (Exception e) {
                throw new NullBotMsgException("""
                        [问答] ❌生成请求出错
                        - 用户: [CQ:at,qq=%s]""".formatted(userId)
                );
            }

            if (raw.contains("REFUSED")) {
                permissionHandler.setUserBan(userId, this.getClass(), BLOCKING_TIME);
                throw new NullBotMsgException("""
                        [问答] 🚫生成问题敏感
                        - 用户: [CQ:at,qq=%s]
                        - 处罚: 封禁功能%s分钟""".formatted(userId, BLOCKING_TIME)
                );
            }

            String answer;
            String question;
            int timeout;
            try {
                JsonNode json = objectMapper.readTree(raw);
                answer = json.get("answer").asText().toUpperCase();
                timeout = json.get("timeout").asInt();
                question = json.get("question").asText();
            } catch (Exception e) {
                throw new NullBotMsgException("""
                        [问答] ❌生成格式异常
                        - 用户: [CQ:at,qq=%s]""".formatted(userId)
                );
            }

            if (answer.isEmpty() || question.isEmpty() || !answer.matches("[A-Za-z]"))
                throw new NullBotMsgException("""
                        [问答] ❌生成内容异常
                        - 用户: [CQ:at,qq=%s]""".formatted(userId)
                );

            String req = """
                    请[CQ:at,qq=%s]回答问题！
                    %s
                    注: 请直接发送选项, 限时%s秒！""".formatted(userId, question, timeout);

            bot.sendGroupMsg(groupId, req, false);

            List<Pair<Long, String>> inputs = botNextInputer
                    .request(BniMode.PS, userId, "[a-zA-Z]", timeout);

            String res;
            if (inputs.isEmpty()) {
                res = "%s回答超时！答案是...%s！".formatted(userName, answer);
            } else if (answer.equalsIgnoreCase(inputs.getFirst().getRight())) {
                res = "%s回答正确！".formatted(userName);
            } else {
                res = "%s回答错误！答案是...%s！".formatted(userName, answer);
            }
            bot.sendGroupMsg(groupId, res, false);

        } finally {
            inGameUsers.remove(userId);
        }
    }

    public boolean switchThinking() {
        return thinking = !thinking;
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
