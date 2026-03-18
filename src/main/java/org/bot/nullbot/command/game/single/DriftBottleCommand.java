package org.bot.nullbot.command.game.single;

import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.control.BotNextInputer;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.info.FileInfo;
import org.bot.nullbot.entity.po.DriftBottlePO;
import org.bot.nullbot.enums.BniMode;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.DriftBottleService;
import org.bot.nullbot.service.FileService;
import org.bot.nullbot.util.DownloadUtil;
import org.bot.nullbot.util.FileUtil;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@CommandMapping({"DriftBottle", "漂流瓶"})
@Component
@RequiredArgsConstructor
@Slf4j
public class DriftBottleCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;
    private final DriftBottleService driftBottleService;
    private final BotNextInputer botNextInputer;

    private static final int KEEP_TIME = 30;  // 漂流瓶保留时间

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();
        String message = event.getMessage();
        Map<String, String> imageMap = MessageParseUtil.parseGroupRawMessageAsImageMap(event.getRawMessage());

        if (!imageMap.isEmpty()) {
            boolean autoThrow = !params.isEmpty() && "-auto".equals(params.getFirst());
            if (imageMap.size() != 1 && !autoThrow)
                throw new NullBotMsgException("[漂流瓶] ❌仅可投单张图片");
            for (Map.Entry<String, String> entry : imageMap.entrySet()) {
                String url = entry.getValue();
                String fileName = UUID.randomUUID().toString();
                String directory = fileStorageProperties.getImagePath() + "/bottle";
                FileInfo fileInfo = null;
                boolean thrown = false;
                try {
                    fileInfo = DownloadUtil.downloadFile(url, directory, fileName, "\t\t\t\t├─ ");
                    if (!fileService.addFileRecordForBot(
                            directory,
                            fileInfo.getFileName(),
                            fileInfo.getFileSize(),
                            fileInfo.getLastModified(),
                            userId, userName)
                    ) {
                        if (!autoThrow) throw new NullBotMsgException("[漂流瓶] ❌数据库更新失败");
                    }
                    thrown = driftBottleService.throwBottle(
                            userId,
                            bot.getStrangerInfo(userId, true).getData().getNickname(),
                            directory + "/" + fileInfo.getFileName(),
                            true
                    );
                    if (!autoThrow) {
                        bot.sendGroupMsg(event.getGroupId(), thrown ? "✉️ 已投出！" : "[漂流瓶] ❌出错", false);
                        log.info("\t\t\t\t├─[DriftBottle] 扔漂流瓶(图片) - {} -> {}", userId, thrown ? "已投出" : "出错");
                    }
                } catch (Exception e) {
                    if (!autoThrow) throw new NullBotMsgException("[漂流瓶] ❌出错: " + e.getMessage());
                } finally {
                    if (fileInfo != null && !thrown) {
                        fileService.deleteFileRecordForBot(directory, fileInfo.getFileName());
                        FileUtil.deleteFileByName(directory, fileInfo.getFileName());
                    }
                }
            }
            return;
        }

        if (!params.isEmpty()) {
            boolean autoThrow = "-auto".equals(params.getFirst());
            boolean thrown = driftBottleService.throwBottle(
                    userId,
                    bot.getStrangerInfo(userId, true).getData().getNickname(),
                    autoThrow ? message.trim() : message.substring(message.indexOf(" ")).trim(),
                    false
            );
            if (!autoThrow) {
                bot.sendGroupMsg(event.getGroupId(), thrown ? "✉️ 已投出！" : "[漂流瓶] ❌出错", false);
                log.info("\t\t\t\t├─[DriftBottle] 扔漂流瓶 - {} -> {}", userId, thrown ? "已投出" : "出错");
            }
            return;
        }

        DriftBottlePO bottle = driftBottleService.pickUpRand();
        if (bottle == null) throw new NullBotMsgException("没有漂流瓶了！");
        bot.sendGroupMsg(groupId, bottle.toString(), false);
        List<Pair<Long, String>> inputs;
        try {
            inputs = botNextInputer.request(BniMode.PS, userId, "扔回去", KEEP_TIME, true);
        } catch (Exception e) {
            throw new NullBotMsgException("[漂流瓶] ❌" + e.getMessage());
        }
        boolean thrownBack = false;
        if (!inputs.isEmpty()) {
            bottle.plusRethrowTimes();
            thrownBack = driftBottleService.throwBottle(bottle);
            bot.sendGroupMsg(groupId, thrownBack ? "✉️ 已投回！" : "[漂流瓶] ❌出错", true);
            log.info("\t\t\t\t├─[DriftBottle] 捡漂流瓶并投回 - {} -> #{}", userId, bottle.getId());
        } else {
            log.info("\t\t\t\t├─[DriftBottle] 捡漂流瓶并销毁 - {} -> #{}", userId, bottle.getId());
        }
        if (!thrownBack && bottle.getIsImage()) {
            int index = bottle.getContent().lastIndexOf("/");
            String fileName = bottle.getContent().substring(index + 1);
            String directory = bottle.getContent().substring(0, index);
            log.info("[DEBUG] {} and {}", directory, fileName);
            fileService.deleteFileRecordForBot(directory, fileName);
            FileUtil.deleteFileByName(directory, fileName);
        }
    }

    public static int getKeepTime() { return KEEP_TIME; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ DriftBottle 命令
                功能: 扔或者捡一个漂流瓶
                限权: %d 级
                格式: DriftBottle [可选: 文本]
                别名: 漂流瓶
                注意: 可发送"扔回去"投回""", getAccess()
        );
    }
}
