package com.zincoid.nullbot.bot.command.image;

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
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.service.file.FileService;
import com.zincoid.nullbot.core.utils.MsgParseUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@CmdMapping({"ImageDel", "删除图片"})
@Component
@RequiredArgsConstructor
public class ImageDeleteCmd implements Cmd {

    private final StorageProperties storageProperties;
    private final FileService fileService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        String directory = storageProperties.getImagePath() + "/collect";
        ArrayMsg reply = event.getArrayMsg().getFirst();
        if (args.hasNext()) {
            deleteFile(bot, event, directory, args.nextFullString());
            return;
        }
        if (reply.getType() == MsgTypeEnum.reply) {
            MsgResp replyMsg = bot.getMsg((int) reply.getLongData("id")).getData();
            Map<String, String> imageMap = MsgParseUtil.extractImgMap(replyMsg.getArrayMsg());
            if (imageMap.isEmpty()) throw new BotWarnException("引用未包含图片");
            imageMap.forEach((name, url) -> {
                String key = name.substring(0, name.lastIndexOf("."));  // QQ图片扩展名错误
                List<FilePO> realFiles = fileService.search(key, directory);
                if (realFiles.size() != 1)
                    throw new BotErrorException("数据异常");
                deleteFile(bot, event, directory, realFiles.getFirst().getFileName());
            });
            return;
        }
        throw new BotWarnException("缺少引用或文件名");
    }

    private void deleteFile(Bot bot, GroupMessageEvent event, String directory, String fileName) {
        fileService.delete(directory, fileName);
        bot.sendGroupMsg(event.getGroupId(), "⚠️图片已删除", false);
        log.info("☑ [ImageDelete] 图片已删除: {}", fileName);
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ ImageDel 命令
                功能: 删除保存的图片
                限权: %d 级
                格式:
                1. ImageDel [文件名]
                2. [引用图片] ImageDel
                别名: 删除图片""", getAccess()
        );
    }
}
