package org.bot.nullbot.command.game.single;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.storage.GuessStorage;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.info.GuessInfo;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.SettingService;
import org.bot.nullbot.service.UserService;
import org.bot.nullbot.util.FileUtil;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;

@CommandMapping({"Guess", "猜角色", "猜"})
@Component
@Slf4j
@RequiredArgsConstructor
public class GuessCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;
    private final SettingService settingService;
    private final GuessStorage guessStorage;
    private final UserService userService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) throws Exception {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if (event.getCommandParameters().isEmpty())
                throw new NullBotMsgException("[猜角色] ❌参数不足");

            Long groupId = groupMessageEvent.getGroupId();
            Long userId = groupMessageEvent.getUserId();
            String userName = groupMessageEvent.getSender().getNickname();
            String param = event.getCommandParameters().getFirst();

            GuessInfo guessInfo = guessStorage.getGuessInfo(groupId);
            if (guessInfo == null){
                // 初始化猜迷
                String acgPath = fileStorageProperties.getImagePath() + "/acg/" + param;

                String characterPath;
                try {
                    characterPath = FileUtil.getRandomFile(acgPath);
                } catch (Exception e) {
                    throw new NullBotMsgException("[猜角色] ❌不存在该类别");  // 目录异常
                }
                if(characterPath == null)
                    throw new NullBotMsgException("[猜角色] ❌该类别下暂无角色");

                String characterName = characterPath
                        .split("/")[characterPath.split("/").length-1]
                        .split("_")[0];
                guessStorage.initGuessInfo(groupId, characterName, characterPath);

                // 获取猜谜图
                String response = MsgUtils.builder()
                        .text("本群题目✨是\n")
                        .img("base64://" + crop(characterPath,
                                settingService.getGuessRatio(groupId),
                                settingService.getGuessPadding(groupId)))
                        .build();
                bot.sendGroupMsg(groupId, response, false);
                log.info("\t\t\t\t├─[Guess] 初始化群猜谜 - {} -> {}", groupId, characterName);
            }else{
                // 判断对错
                if("-f".equals(param)){
                    bot.sendGroupMsg(groupId, "已放弃\uD83D\uDCA6 答案是...\n" + guessInfo.getName() + "！", false);
                    guessStorage.removeGuess(groupId);
                    log.info("\t\t\t\t├─[Guess] 放弃猜测 - {}", userId);
                    return;
                }
                guessStorage.increaseTimes(groupId);
                if(guessInfo.getName().equals(param)){
                    userService.plusExperience(userId, 20);  // 给赢家20Exp
                    userService.increaseDrawTimes(userId, 5);  // 给赢家5抽
                    String response = MsgUtils.builder()
                            .text(userName + "猜对啦✨\n答案是..." + guessInfo.getName() + "！\n- 获得 5抽数 和 20Exp！\n- 一共猜了" + guessInfo.getTimes() + "次！")
                            .img(guessInfo.getPath())
                            .build();
                    bot.sendGroupMsg(groupId, response, false);
                    guessStorage.removeGuess(groupId);
                    log.info("\t\t\t\t├─[Guess] 猜测正确 - {}", userId);
                }else{
                    if(guessInfo.getTimes() >= 10){
                        bot.sendGroupMsg(groupId, "错了10次啦！答案是...\n" + guessInfo.getName() + "！", false);
                        guessStorage.removeGuess(groupId);
                        log.info("\t\t\t\t├─[Guess] 猜测错误 已超过最大尝试次数 - {}", userId);
                    }else{
                        bot.sendGroupMsg(groupId, "猜错啦！", false);
                        log.info("\t\t\t\t├─[Guess] 猜测错误 - {}", userId);
                    }
                }
            }
        }else
            throw new NullBotLogException("[猜] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Guess 命令
                功能: 猜角色
                奖励: 1抽数 & 10Exp
                限权: %d 级
                格式: Guess [人物来源|人物名|-f(放弃)]
                别名: 猜角色/猜""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ Guess 命令
                功能: 猜角色
                限权: %d 级
                格式: Guess [人物来源|-f(放弃)]
                人物来源: 明日方舟
                示例: Guess 明日方舟""", getAccess()
        );
    }

    // public static String crop(String p, double r) throws Exception {
    //     BufferedImage i = ImageIO.read(new File(p));
    //     int w=(int)(i.getWidth()*r), h=(int)(i.getHeight()*r);
    //     int x=i.getWidth()>w?(int)(Math.random()*(i.getWidth()-w)):0;
    //     int y=i.getHeight()>h?(int)(Math.random()*(i.getHeight()-h)):0;
    //     BufferedImage c=i.getSubimage(x,y,w,h);
    //     ByteArrayOutputStream b=new ByteArrayOutputStream();
    //     ImageIO.write(c,"png",b);
    //     return Base64.getEncoder().encodeToString(b.toByteArray());
    // }

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
}
