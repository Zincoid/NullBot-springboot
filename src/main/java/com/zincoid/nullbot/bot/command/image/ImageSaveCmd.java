package com.zincoid.nullbot.bot.command.image;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.model.information.FileInfo;
import com.zincoid.nullbot.core.service.file.FileService;
import com.zincoid.nullbot.core.utils.MsgParseUtil;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@CmdMapping({"ImageSave", "保存图片"})
@Component
@RequiredArgsConstructor
public class ImageSaveCmd implements Cmd {

    private final StorageProperties storageProperties;
    private final FileService fileService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        ArrayMsg reply = event.getArrayMsg().getFirst();
        if (reply.getType() != MsgTypeEnum.reply)
            throw new BotWarnException("缺少图片引用");
        MsgResp replyMsg = bot.getMsg((int) reply.getLongData("id")).getData();
        Map<String, String> imageMap = MsgParseUtil.extractImgMap(replyMsg.getArrayMsg());
        if (imageMap.isEmpty())
            throw new BotWarnException("引用未包含图片");

        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        imageMap.forEach((name, url) -> {
            String key = name.substring(0, name.lastIndexOf("."));  // QQ图片扩展名错误
            String filePath = storageProperties.getImagePath() + "/collect";
            FileInfo fileInfo = fileService.upload(url, filePath, key, userId);
            bot.sendGroupMsg(groupId, "\uD83D\uDCBD图片已保存", false);
            log.info("☑ [ImageSave] 图片已保存: {}", fileInfo.getName());
        });
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ ImageSave 命令
                功能: 保存图片至图库
                限权: %d 级
                格式: [引用图片] ImageSave
                别名: 保存图片""", getAccess()
        );
    }
}
