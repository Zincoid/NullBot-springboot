package com.zincoid.nullbot.bot.command.game.single;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.control.BotInputManager;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.model.information.FileInfo;
import com.zincoid.nullbot.core.model.data.po.DriftBottlePO;
import com.zincoid.nullbot.core.enums.BniMode;
import com.zincoid.nullbot.core.service.DriftBottleService;
import com.zincoid.nullbot.core.service.FileService;
import com.zincoid.nullbot.core.util.MsgParseUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@CommandMapping({"DriftBottle", "漂流瓶"})
@Component
@RequiredArgsConstructor
public class DriftBottleCommand implements Command {

    private static final int KEEP_TIME = 30;  // 漂流瓶保留时间

    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;
    private final DriftBottleService driftBottleService;
    private final BotInputManager botInputManager;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();
        String message = event.getMessage();
        Map<String, String> imageMap = MsgParseUtil.extractImgMap(event.getRawMessage());

        if (!imageMap.isEmpty()) {
            boolean autoThrow = !args.isEmpty() && "-auto".equals(args.nextString());
            if (imageMap.size() != 1 && !autoThrow)
                throw new NullBotException("仅可投单张图片");
            for (Map.Entry<String, String> entry : imageMap.entrySet()) {
                String url = entry.getValue();
                String fileName = UUID.randomUUID().toString();
                String directory = fileStorageProperties.getImagePath() + "/bottle";
                FileInfo fileInfo = null;
                boolean thrown = false;
                try {
                    fileInfo = fileService.saveFile(url, directory, fileName, userId, userName);
                    thrown = driftBottleService.throwBottle(
                            userId,
                            bot.getStrangerInfo(userId, true).getData().getNickname(),
                            directory + "/" + fileInfo.getFileName(),
                            true
                    );
                    if (!autoThrow) {
                        bot.sendGroupMsg(event.getGroupId(), thrown ? "✉️ 已投出！" : "❌出错", false);
                        log.info("☑ [DriftBottle] 扔漂流瓶(图片) - {} -> {}", userId, thrown ? "已投出" : "出错");
                    }
                } finally {
                    if (fileInfo != null && !thrown) {
                        fileService.deleteFile(directory, fileInfo.getFileName());
                    }
                }
            }
            return;
        }

        if (!args.isEmpty()) {
            boolean autoThrow = "-auto".equals(args.nextString());
            boolean thrown = driftBottleService.throwBottle(
                    userId,
                    bot.getStrangerInfo(userId, true).getData().getNickname(),
                    autoThrow ? message.trim() : message.substring(message.indexOf(" ")).trim(),
                    false
            );
            if (!autoThrow) {
                bot.sendGroupMsg(event.getGroupId(), thrown ? "✉️ 已投出！" : "❌出错", false);
                log.info("☑ [DriftBottle] 扔漂流瓶 - {} -> {}", userId, thrown ? "已投出" : "出错");
            }
            return;
        }

        DriftBottlePO bottle = driftBottleService.pickUpRand();
        if (bottle == null)
            throw new NullBotException("没有漂流瓶了！");
        bot.sendGroupMsg(groupId, bottle.toString(), false);

        List<Pair<Long, String>> inputs = botInputManager
                .request(BniMode.PS, userId, "扔回去", KEEP_TIME, true);

        boolean thrownBack = false;
        if (!inputs.isEmpty()) {
            bottle.plusRethrowTimes();
            thrownBack = driftBottleService.throwBottle(bottle);
            bot.sendGroupMsg(groupId, thrownBack ? "✉️ 已投回！" : "❌投回出错", true);
            log.info("☑ [DriftBottle] 捡漂流瓶并投回 - {} -> #{}", userId, bottle.getId());
        } else {
            log.info("☑ [DriftBottle] 捡漂流瓶并销毁 - {} -> #{}", userId, bottle.getId());
        }

        if (!thrownBack && bottle.getIsImage()) {
            int index = bottle.getContent().lastIndexOf("/");
            String fileName = bottle.getContent().substring(index + 1);
            String directory = bottle.getContent().substring(0, index);
            fileService.deleteFile(directory, fileName);
        }
    }

    public static int getKeepTime() { return KEEP_TIME; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ DriftBottle 命令
                功能: 扔或者捡一个漂流瓶
                限权: %d 级
                格式: DriftBottle [可选: 文本/图片]
                别名: 漂流瓶
                注意:
                1. 可发送"扔回去"投回
                2. 投图片时指令后也需空格""", getAccess()
        );
    }
}
