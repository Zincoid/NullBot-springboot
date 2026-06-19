package com.zincoid.nullbot.bot.command.game.indie;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.bot.exception.BotOmitException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.module.control.BotInputManager;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.model.information.FileInfo;
import com.zincoid.nullbot.core.model.data.po.BottlePO;
import com.zincoid.nullbot.core.enums.BniMode;
import com.zincoid.nullbot.core.service.game.BottleService;
import com.zincoid.nullbot.core.service.file.FileService;
import com.zincoid.nullbot.core.utils.MsgUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@CmdMapping({"Bottle", "漂流瓶"})
@Component
@RequiredArgsConstructor
public class BottleCmd implements Cmd {

    private static final int KEEP_TIMEOUT_SECONDS = 30;  // 漂流瓶保留时间

    private final StorageProperties storageProperties;
    private final FileService fileService;
    private final BottleService bottleService;
    private final BotInputManager botInputManager;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String message = event.getMessage();
        boolean autoThrow = args.hasOpt("auto", "a");
        Map<String, String> imageMap = MsgUtil.extractImgMap(event.getArrayMsg());

        if (!imageMap.isEmpty()) {
            if (imageMap.size() != 1) {
                if (autoThrow) throw new BotOmitException("图片过多");
                throw new BotWarnException("图片过多");
            }
            String imageUrl = imageMap.entrySet().iterator().next().getValue();
            String imageName = UUID.randomUUID().toString();
            String bottlePath = storageProperties.getImagePath() + "/bottle";
            FileInfo fileInfo = null;
            boolean thrown = false;
            try {
                fileInfo = fileService.upload(imageUrl, bottlePath, imageName, userId);
                thrown = bottleService.add(
                        userId,
                        bot.getStrangerInfo(userId, true).getData().getNickname(),
                        fileInfo.getPath(),
                        true
                );
                if (!autoThrow)
                    bot.sendGroupMsg(event.getGroupId(), thrown ? "✉️已投出" : "❌未投出", false);
                log.info("☑ [Bottle] 扔漂流瓶(图片) - {} -> {}", userId, thrown);
            } finally {
                if (fileInfo != null && !thrown)
                    fileService.delete(bottlePath, fileInfo.getName());
            }
            return;
        }

        if (args.hasNext()) {
            boolean thrown = bottleService.add(
                    userId,
                    bot.getStrangerInfo(userId, true).getData().getNickname(),
                    autoThrow ? message.trim() : args.rest(),
                    false
            );
            if (!autoThrow)
                bot.sendGroupMsg(event.getGroupId(), thrown ? "✉️已投出" : "❌未投出", false);
            log.info("☑ [Bottle] 扔漂流瓶 - {} -> {}", userId, thrown);
            return;
        }

        BottlePO bottle = bottleService.pick();
        if (bottle == null)
            throw new BotInfoException(Emoji.INFO, "没有漂流瓶了");
        bot.sendGroupMsg(groupId, bottle.toString(), false);

        List<Pair<Long, String>> inputs = botInputManager
                .request(BniMode.PS, userId, "扔回去", KEEP_TIMEOUT_SECONDS, true);

        boolean thrownBack = false;
        if (!inputs.isEmpty()) {
            bottle.plusRethrowTimes();
            thrownBack = bottleService.add(bottle);
            bot.sendGroupMsg(groupId, thrownBack ? "✉️已投回" : "❌未投回", true);
            log.info("☑ [Bottle] 捡漂流瓶并投回 - {} -> #{}", userId, bottle.getId());
        } else {
            log.info("☑ [Bottle] 捡漂流瓶并销毁 - {} -> #{}", userId, bottle.getId());
        }

        if (!thrownBack && bottle.getIsImage()) {
            int index = bottle.getContent().lastIndexOf("/");
            String fileName = bottle.getContent().substring(index + 1);
            String directory = bottle.getContent().substring(0, index);
            fileService.delete(directory, fileName);
        }
    }

    public static int getKeepTimeoutSeconds() { return KEEP_TIMEOUT_SECONDS; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Bottle 命令
                功能: 扔或捡漂流瓶
                限权: %d 级
                格式: Bottle [可选: 文本/图片]
                别名: 漂流瓶
                注意:
                1. 可发送"扔回去"投回
                2. 投图片时指令后也需空格""", getAccess()
        );
    }
}
