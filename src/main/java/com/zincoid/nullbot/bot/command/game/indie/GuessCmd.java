package com.zincoid.nullbot.bot.command.game.indie;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.module.control.BotInputManager;
import com.zincoid.nullbot.core.module.storage.GuessStorage;
import com.zincoid.nullbot.core.model.information.GuessInfo;
import com.zincoid.nullbot.core.model.data.po.SettingPO;
import com.zincoid.nullbot.core.enums.BniMode;
import com.zincoid.nullbot.core.service.base.UserService;
import com.zincoid.nullbot.core.utils.Base64Util;
import com.zincoid.nullbot.core.context.BotCtx;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

@Slf4j
@CmdMapping({"Guess", "猜角色", "猜"})
@Component
@RequiredArgsConstructor
public class GuessCmd implements Cmd {

    private static final int WAIT_TIMEOUT_SECONDS = 99;  // 等待超时时间
    private static final int MAX_RETRIES = 10;  // 最大回答次数
    private static final int MAX_CROP_ATTEMPTS = 100;  // 切图尝试限制

    private final BotInputManager botInputManager;
    private final GuessStorage guessStorage;
    private final UserService userService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) throws Exception {
        Long groupId = event.getGroupId();

        if (args.hasOpt("forfeit", "f")) {
            if (guessStorage.getGuess(groupId) == null)
                throw new BotInfoException(Emoji.WARN, "未在游戏中");
            botInputManager.cancelWait(BniMode.GS, groupId);
            log.info("☑ [Guess] 猜谜已放弃 - GroupId: {}", groupId);
            return;
        }

        if (guessStorage.getGuess(groupId) != null)
            throw new BotInfoException(Emoji.INFO, "已在游戏中");

        try {
            GuessInfo guess = guessStorage.initGuess(groupId, args.next());
            SettingPO setting = BotCtx.getSetting();

            String start = MsgUtils.builder()
                    .text("[猜角色] ✨题目是\n")
                    .img("base64://" + crop(guess.getPath(),
                            setting.getGuessCropRatio(),
                            setting.getGuessPadding(),
                            setting.getGuessTransparentRatio()
                    ))
                    .text("注: 请发送\"#内容\"")
                    .build();
            bot.sendGroupMsg(groupId, start, false);
            log.info("☑ [Guess] 猜谜已初始化 - {} -> {}", groupId, guess.getName());

            while (guess.getTimes() < MAX_RETRIES) {
                guessStorage.increaseTimes(groupId);
                List<Pair<Long, String>> inputs = botInputManager
                        .request(BniMode.GS, groupId, "#.+", WAIT_TIMEOUT_SECONDS);

                if (inputs.isEmpty() || "##".equals(inputs.getFirst().getRight())) {
                    String end = MsgUtils.builder()
                            .text("""
                                    游戏结束啦\uD83D\uDCA6
                                    答案是...%s！""".formatted(guess.getName()))
                            .img("base64://" + Base64Util.from(guess.getPath()))
                            .build();
                    bot.sendGroupMsg(groupId, end, false);
                    log.info("☑ [Guess] 猜谜已结束 - GroupId: {}", groupId);
                    return;
                }

                Long answererId = inputs.getFirst().getLeft();
                String answer = inputs.getFirst().getRight().substring(1).trim();

                if (guess.getName().equals(answer)) {
                    boolean rewardable = userService.exist(answererId);
                    if (rewardable) {
                        userService.plusExperience(answererId, 20);  // 给赢家 20 Exp
                        userService.increaseDrawTimes(answererId, 5);  // 给赢家 5 抽
                    }
                    String correct = MsgUtils.builder()
                            .text("""
                                    %s猜对啦✨
                                    答案是...%s！
                                    - %s
                                    - 一共猜了%s次"""
                                    .formatted(bot.getStrangerInfo(answererId, true).getData().getNickname(), answer,
                                            rewardable ? "获得 5抽数 和 20Exp！" : "无奖励: 用户未注册",
                                            guess.getTimes()))
                            .img("base64://" + Base64Util.from(guess.getPath()))
                            .build();
                    bot.sendGroupMsg(groupId, correct, false);
                    log.info("☑ [Guess] 猜测正确 - UserId: {}", answererId);
                    return;
                } else {
                    bot.sendGroupMsg(groupId, "[CQ:at,qq=%s] 猜错啦！".formatted(answererId), false);
                    log.info("☑ [Guess] 猜测错误 - UserId: {}", answererId);
                }
            }

            String fail = MsgUtils.builder()
                    .text("""
                            已经错%s次啦\uD83D\uDCA6
                            答案是...%s！""".formatted(MAX_RETRIES, guess.getName()))
                    .img("base64://" + Base64Util.from(guess.getPath()))
                    .build();
            bot.sendGroupMsg(groupId, fail, false);
            log.info("☑ [Guess] 猜谜已超限 - GroupId: {}, MaxRetries: {}", groupId, MAX_RETRIES);

        } finally {
            guessStorage.removeGuess(groupId);
        }
    }

    private static String crop(String imagePath, double cropRatio, int padding,
                               double transparentRatio) throws Exception {
        BufferedImage img = ImageIO.read(new File(imagePath));
        // 计算裁剪尺寸 确保在padding内部
        int w = Math.max(1, (int) (img.getWidth() * cropRatio));
        int h = Math.max(1, (int) (img.getHeight() * cropRatio));
        w = Math.min(w, img.getWidth() - 2 * padding);
        h = Math.min(h, img.getHeight() - 2 * padding);
        // 计算可裁剪范围
        int xMin = Math.max(0, padding);
        int xMax = Math.max(xMin, img.getWidth() - w - padding);
        int yMin = Math.max(0, padding);
        int yMax = Math.max(yMin, img.getHeight() - h - padding);
        // 没有Alpha通道 直接返回随机裁剪
        if (!img.getColorModel().hasAlpha()) {
            int x = xMin + (xMax > xMin ? (int) (Math.random() * (xMax - xMin)) : 0);
            int y = yMin + (yMax > yMin ? (int) (Math.random() * (yMax - yMin)) : 0);
            return Base64Util.from(img.getSubimage(x, y, w, h));
        }
        int attempts = 0;
        while (attempts < MAX_CROP_ATTEMPTS) {
            // 随机选择裁剪起点
            int x = xMin + (xMax > xMin ? (int) (Math.random() * (xMax - xMin)) : 0);
            int y = yMin + (yMax > yMin ? (int) (Math.random() * (yMax - yMin)) : 0);
            BufferedImage subImg = img.getSubimage(x, y, w, h);
            // 计算透明像素比例
            int transparentCount = 0;
            int[] pixels = subImg.getRGB(0, 0, w, h, null, 0, w);
            for (int pixel : pixels)
                if (((pixel >> 24) & 0xff) == 0)  // 计入透明像素的阈值
                    transparentCount++;
            double ratio = (double) transparentCount / (w * h);
            if (ratio <= transparentRatio)
                return Base64Util.from(subImg);
            attempts++;
        }
        throw new BotWarnException("经过%s次尝试后仍未找到透明像素比例小于%s的切图".formatted(
                GuessCmd.MAX_CROP_ATTEMPTS, transparentRatio
        ));
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Guess 命令
                功能: 猜角色
                奖励: 5抽数 & 20Exp
                限权: %d 级
                用法: Guess [选项] [类别]

                选项:
                  -f, --forfeit   放弃当前游戏

                别名: 猜角色/猜
                注意:
                1. 回答格式为#加你的猜测
                2. 回答时也可使用##放弃""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ Guess 命令
                功能: 猜角色
                用法: Guess [选项] [类别]

                选项:
                  -f, --forfeit   放弃当前游戏

                类别: 明日方舟
                示例: Guess 明日方舟""";
    }
}
