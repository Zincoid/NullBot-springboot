package org.bot.nullbot.command.game.single;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.control.BotNextInputer;
import org.bot.nullbot.component.storage.GuessStorage;
import org.bot.nullbot.entity.info.GuessInfo;
import org.bot.nullbot.enums.BniMode;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.SettingService;
import org.bot.nullbot.service.UserService;
import org.bot.nullbot.util.Base64Util;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import java.util.List;

@CommandMapping({"Guess", "猜角色", "猜"})
@Component
@Slf4j
@RequiredArgsConstructor
public class GuessCommand implements Command
{
    private final BotNextInputer botNextInputer;
    private final SettingService settingService;
    private final GuessStorage guessStorage;
    private final UserService userService;

    private static final int WAIT_TIMEOUT = 99;  // 超时时间 单位: Second
    private static final int MAX_RETRIES = 10;  // 最大尝试次数

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) throws Exception {
        Long groupId = event.getGroupId();
        if (params.isEmpty())
            throw new NullBotMsgException("[猜角色] ❌参数不足");

        if ("-f".equals(params.getFirst())) {
            if (guessStorage.getGuess(groupId) == null)
                throw new NullBotMsgException("[猜角色] ❌未在游戏中");
            botNextInputer.cancelWait(BniMode.GS, groupId);
            log.info("\t\t\t\t├─[Guess] 群聊 {} 放弃猜测", groupId);
            return;
        }

        if (guessStorage.getGuess(groupId) != null)
            throw new NullBotMsgException("[猜角色] ⚠️已在游戏中");

        GuessInfo guess;
        try {
            guess = guessStorage.initGuess(groupId, params.getFirst());
        } catch (Exception e) {
            throw new NullBotMsgException("[猜角色] ❌" + e.getMessage());
        }

        String startMsg = MsgUtils.builder().text("[猜角色] ✨题目是\n")
                .img("base64://" + crop(guess.getPath(),
                        settingService.getGuessRatio(groupId),
                        settingService.getGuessPadding(groupId)))
                .text("注: 请发送\"#内容\"")
                .build();
        bot.sendGroupMsg(groupId, startMsg, false);
        log.info("\t\t\t\t├─[Guess] 群聊 {} 初始化猜谜 -> {}", groupId, guess.getName());

        while (guess.getTimes() < MAX_RETRIES) {
            guessStorage.increaseTimes(groupId);
            List<Pair<Long, String>> inputs = botNextInputer
                    .request(BniMode.GS, groupId, WAIT_TIMEOUT, "#.+");

            if (inputs.isEmpty()) {
                guessStorage.removeGuess(groupId);
                bot.sendGroupMsg(groupId, """
                        已经结束啦\uD83D\uDCA6
                        答案是...%s！""".formatted(guess.getName()), false);
                log.info("\t\t\t\t├─[Guess] 群聊 {} 已结束", groupId);
                return;
            }

            Long answererId = inputs.getFirst().getLeft();
            String answererName = bot.getStrangerInfo(answererId, true).getData().getNickname();
            String answer = inputs.getFirst().getRight().substring(1);

            if (guess.getName().equals(answer)) {
                userService.plusExperience(answererId, 20);  // 给赢家 20 Exp
                userService.increaseDrawTimes(answererId, 5);  // 给赢家 5 抽
                String endMsg = MsgUtils.builder().text("""
                                %s猜对啦✨
                                答案是...%s！
                                - 获得 5抽数 和 20Exp！
                                - 一共猜了%s次！""".formatted(answererName, answer, guess.getTimes()))
                        .img(guess.getPath())
                        .build();
                guessStorage.removeGuess(groupId);
                bot.sendGroupMsg(groupId, endMsg, false);
                log.info("\t\t\t\t├─[Guess] 用户 {} 猜测正确", answererId);
                return;
            } else {
                bot.sendGroupMsg(groupId, "猜错啦！", false);
                log.info("\t\t\t\t├─[Guess] 用户 {} 猜测错误", answererId);
            }
        }

        guessStorage.removeGuess(groupId);
        bot.sendGroupMsg(groupId, """
                    已经错%s次啦\uD83D\uDCA6
                    答案是...%s！""".formatted(MAX_RETRIES, guess.getName()), false);
        log.info("\t\t\t\t├─[Guess] 群聊 {} 已超过最大尝试次数: {}", groupId, MAX_RETRIES);
    }

    public static String crop(String p, double r, int pad) throws Exception {
        BufferedImage img = ImageIO.read(new File(p));
        // 计算裁剪尺寸 确保在 padding 内部
        int w = Math.max(1, (int)(img.getWidth() * r));
        int h = Math.max(1, (int)(img.getHeight() * r));
        // 调整裁剪尺寸以适应 padding
        w = Math.min(w, img.getWidth() - 2 * pad);
        h = Math.min(h, img.getHeight() - 2 * pad);
        // 计算可裁剪范围
        int xMin = Math.max(0, pad);
        int xMax = Math.max(xMin, img.getWidth() - w - pad);
        int yMin = Math.max(0, pad);
        int yMax = Math.max(yMin, img.getHeight() - h - pad);
        // 随机选择裁剪起点
        int x = xMin + (xMax > xMin ? (int)(Math.random() * (xMax - xMin)) : 0);
        int y = yMin + (yMax > yMin ? (int)(Math.random() * (yMax - yMin)) : 0);
        // 裁剪并转换 Base64
        return Base64Util.imageToBase64(img.getSubimage(x, y, w, h));
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Guess 命令
                功能: 猜角色
                奖励: 5抽数 & 20Exp
                限权: %d 级
                格式: Guess [类别|-f(放弃)]
                别名: 猜角色/猜
                注意: 回答格式为#加你的猜测""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ Guess 命令
                功能: 猜角色
                格式: Guess [类别|-f(放弃)]
                类别: 明日方舟
                示例: Guess 明日方舟""";
    }
}
