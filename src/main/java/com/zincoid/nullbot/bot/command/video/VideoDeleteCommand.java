package com.zincoid.nullbot.bot.command.video;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.service.file.FileService;
import com.zincoid.nullbot.core.utils.MsgParseUtil;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@CommandMapping({"VideoDel", "删除视频"})
@Component
@RequiredArgsConstructor
public class VideoDeleteCommand implements Command {

    private final StorageProperties storageProperties;
    private final FileService fileService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        String directory = storageProperties.getVideoPath() + "/collect";
        ArrayMsg reply = event.getArrayMsg().getFirst();
        if (args.hasNext()) {
            deleteFile(bot, event, directory, args.nextFullString());
            return;
        }
        if (reply.getType() == MsgTypeEnum.reply) {
            MsgResp replyMsg = bot.getMsg((int) reply.getLongData("id")).getData();
            Map<String, String> videoMap = MsgParseUtil.extractVidMap(replyMsg.getArrayMsg());
            if (videoMap.isEmpty()) throw new BotWarnException("引用未包含视频");
            videoMap.forEach((name, url) -> deleteFile(bot, event, directory, name));
            return;
        }
        throw new BotWarnException("缺少引用或文件名");
    }

    private void deleteFile(Bot bot, GroupMessageEvent event, String directory, String fileName) {
        fileService.delete(directory, fileName);
        bot.sendGroupMsg(event.getGroupId(), "⚠️视频已删除", false);
        log.info("☑ [VideoDelete] 视频已删除: {}", fileName);
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ VideoDel 命令
                功能: 删除保存的视频
                限权: %d 级
                格式:
                1. VideoDel [文件名]
                2. [引用视频] VideoDel
                别名: 删除视频""", getAccess()
        );
    }
}
