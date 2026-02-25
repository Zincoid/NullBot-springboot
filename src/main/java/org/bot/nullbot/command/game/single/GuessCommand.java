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

    private static final int GUESS_TIMEOUT = 99;  // 超时时间 单位: Second

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) throws Exception {
        if (params.isEmpty())
            throw new NullBotMsgException("[猜角色] ❌参数不足");

        Long groupId = event.getGroupId();
        String param = params.getFirst();

        if ("-f".equals(param)) {
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
            guess = guessStorage.initGuess(groupId, param);
        } catch (Exception e) {
            throw new NullBotMsgException("[猜角色] ❌" + e.getMessage());
        }

        String startMsg = MsgUtils.builder()
                .text("[猜角色] ✨题目如下\n")
                .img("base64://" + crop(guess.getPath(),
                        settingService.getGuessRatio(groupId),
                        settingService.getGuessPadding(groupId)))
                .text("注: 发送\"#内容\"来猜测(%s秒内)".formatted(GUESS_TIMEOUT))
                .build();
        bot.sendGroupMsg(groupId, startMsg, false);
        log.info("\t\t\t\t├─[Guess] 群聊 {} 初始化猜谜 -> {}", groupId, guess.getName());

        do {
            List<Pair<Long, String>> inputs = botNextInputer
                    .request(BniMode.GS, groupId, GUESS_TIMEOUT, "#.+");

            if (inputs.isEmpty()) {
                guessStorage.removeGuess(groupId);
                bot.sendGroupMsg(groupId, "已结束\uD83D\uDCA6 答案是...\n" + guess.getName() + "！", false);
                log.info("\t\t\t\t├─[Guess] 群聊 {} 已结束", groupId);
                return;
            }

            Long answererId = inputs.getFirst().getLeft();
            String answererName = bot.getStrangerInfo(answererId, true).getData().getNickname();
            String answer = inputs.getFirst().getRight().substring(1);

            guessStorage.increaseTimes(groupId);
            if (guess.getName().equals(answer)) {
                userService.plusExperience(answererId, 20);  // 给赢家 20 Exp
                userService.increaseDrawTimes(answererId, 5);  // 给赢家 5 抽
                String response = MsgUtils.builder()
                        .text("""
                                %s猜对啦✨
                                答案是...%s！
                                - 获得 5抽数 和 20Exp！
                                - 一共猜了%s次！""".formatted(answererName, answer, guess.getTimes()))
                        .img(guess.getPath())
                        .build();
                bot.sendGroupMsg(groupId, response, false);
                guessStorage.removeGuess(groupId);
                log.info("\t\t\t\t├─[Guess] 用户 {} 猜测正确", answererId);
                break;
            } else {
                bot.sendGroupMsg(groupId, "猜错啦！", false);
                log.info("\t\t\t\t├─[Guess] 用户 {} 猜测错误", answererId);
            }
        } while (guess.getTimes() < 10);

        if (guess.getTimes() >= 10) {
            bot.sendGroupMsg(groupId, "错了10次啦！答案是...\n" + guess.getName() + "！", false);
            guessStorage.removeGuess(groupId);
            log.info("\t\t\t\t├─[Guess] 群聊 {} 已超过最大尝试次数", groupId);
        }

        guessStorage.removeGuess(groupId);
    }

    public static String crop(String p, double r, int pad) throws Exception {
        BufferedImage img = ImageIO.read(new File(p));

        // 计算裁剪尺寸，确保在padding内部
        int w = Math.max(1, (int)(img.getWidth() * r));
        int h = Math.max(1, (int)(img.getHeight() * r));

        // 调整裁剪尺寸以适应padding
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

        // 裁剪并转换为base64
        BufferedImage crop = img.getSubimage(x, y, w, h);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(crop, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Guess 命令
                功能: 猜角色
                奖励: 5抽数 & 20Exp
                限权: %d 级
                格式: Guess [类别|-f(放弃)]
                别名: 猜角色/猜""", getAccess()
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
