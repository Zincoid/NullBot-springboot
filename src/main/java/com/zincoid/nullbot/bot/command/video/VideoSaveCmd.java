package com.zincoid.nullbot.bot.command.video;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotErrorException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.model.information.FileMeta;
import com.zincoid.nullbot.core.service.file.FileService;
import com.zincoid.nullbot.core.utils.MsgUtil;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@CmdMapping({"VideoSave", "保存视频"})
@Component
@RequiredArgsConstructor
public class VideoSaveCmd implements Cmd {

    private final StorageProperties storageProperties;
    private final FileService fileService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        ArrayMsg reply = event.getArrayMsg().getFirst();
        if (reply.getType() != MsgTypeEnum.reply)
            throw new BotWarnException("缺少视频引用");

        MsgResp replyMsg = bot.getMsg((int) reply.getLongData("id")).getData();
        // 可优化为单个键值对?
        Map<String, String> videoMap = MsgUtil.extractVidMap(replyMsg.getArrayMsg());
        if (videoMap.isEmpty())
            throw new BotWarnException("引用未包含视频");
        if (videoMap.size() > 1)
            throw new BotErrorException("引用视频过多");

        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        Map.Entry<String, String> entry = videoMap.entrySet().iterator().next();
        String fileName = !args.hasNext() ? entry.getKey()
                : args.rest() + "." + entry.getKey().split("\\.")[1];
        if (fileName.matches(".*[\\\\/:*?\"<>|].*"))
            throw new BotWarnException("文件名非法");
        String filePath = storageProperties.getVideoPath() + "/collect";
        String url = entry.getValue();
        FileMeta fileMeta = fileService.upload(url, filePath, fileName, userId);
        bot.sendGroupMsg(groupId, "\uD83C\uDFA5视频已保存", false);
        log.info("☑ [VideoSave] 视频已保存: {}", fileMeta.getName());
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ VideoSave 命令
                功能: 保存视频
                限权: %d 级
                格式: [引用] VideoSave [可选: 文件名]
                别名: 保存视频""", getAccess()
        );
    }
}
