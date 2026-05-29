package com.zincoid.nullbot.bot.command.video;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.service.file.FileService;
import com.zincoid.nullbot.core.util.MsgParseUtil;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@CommandMapping({"VideoDel", "删除视频"})
@Component
@RequiredArgsConstructor
public class VideoDeleteCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        String directory = fileStorageProperties.getVideoPath() + "/collect";
        ArrayMsg reply = event.getArrayMsg().getFirst();
        if (args.hasNext()) {
            deleteFile(bot, event, directory, args.nextFullString());
            return;
        }
        if (reply.getType() == MsgTypeEnum.reply) {
            MsgResp replyMsg = bot.getMsg(reply.getData().get("id").asInt()).getData();
            Map<String, String> videoMap = MsgParseUtil.extractVidMap(replyMsg.getRawMessage());
            if (videoMap.isEmpty()) throw new BotWarnException("引用未包含视频");
            videoMap.forEach((name, url) -> deleteFile(bot, event, directory, name));
            return;
        }
        throw new BotWarnException("缺少引用或文件名");
    }

    private void deleteFile(Bot bot, GroupMessageEvent event, String directory, String fileName) {
        if(!fileService.deleteFile(directory, fileName))
            throw new BotInfoException(Emoji.WARN, "视频删除失败");
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
