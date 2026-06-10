package com.zincoid.nullbot.bot.command.game.single;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.core.model.bot.args.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.component.ai.chat.message.BaseMessage;
import com.zincoid.nullbot.core.component.ai.chat.model.OpenAiModel;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.control.BotInputManager;
import com.zincoid.nullbot.bot.dispatcher.handler.impl.PermissionHandler;
import com.zincoid.nullbot.core.enums.BniMode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@CommandMapping({"Question", "问答"})
@Component
@RequiredArgsConstructor
public class QuestionCommand implements Command {

    private static final int BLOCK_TIME_MINUTES = 1;  // 封禁时间

    private boolean thinking = true;  // 思考模式

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<Long> inGameUsers = new ConcurrentHashSet<>();

    private final OpenAiModel openAiModel;
    private final BotInputManager botInputManager;
    private final PermissionHandler permissionHandler;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();

        if (inGameUsers.contains(userId))
            throw new BotInfoException(Emoji.INFO, "已在游戏中");

        try {
            inGameUsers.add(userId);
            bot.sendGroupMsg(groupId, "⏳问题生成中...", false);

            String raw;
            try {
                raw = openAiModel.invoke(
                        List.of(BaseMessage.system("""
                                出一道单选题并给出题目和答案，问题主题：%s。请严格按照以下JSON格式回复，不要包含任何其他内容：
                                {"answer":"正确选项字母","timeout":回答限时秒数,"question":"题目内容(选项要换行)"}
                                注:
                                1. timeout根据题目难度设定，简单题15-30秒，中等题45-60秒，困难题90-120秒，
                                2. 公式相关内容不要使用Latex格式
                                3. 禁止生成中国国内政治事件和政治人物相关问题，当主题涉及时仅回复REFUSED"""
                                .formatted(args.nextFullString("二次元")))),
                        thinking, 2500
                ).getContent();
            } catch (Exception e) {
                throw new BotWarnException("生成请求出错");
            }

            if (raw.contains("REFUSED")) {
                permissionHandler.setUserBan(userId, this.getClass(), BLOCK_TIME_MINUTES);
                throw new BotInfoException(Emoji.WARN, "生成问题敏感(封禁%sMin)".formatted(BLOCK_TIME_MINUTES));
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
                throw new BotWarnException("生成格式异常");
            }

            if (answer.isEmpty() || question.isEmpty() || !answer.matches("[A-Za-z]"))
                throw new BotWarnException("生成内容异常");

            String req = """
                    请[CQ:at,qq=%s]回答问题！
                    %s
                    注: 请直接发送选项, 限时%s秒！""".formatted(userId, question, timeout);

            bot.sendGroupMsg(groupId, req, false);

            List<Pair<Long, String>> inputs = botInputManager
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
