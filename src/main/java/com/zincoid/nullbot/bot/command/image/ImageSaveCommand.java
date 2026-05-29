package com.zincoid.nullbot.bot.command.image;

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
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.model.information.FileInfo;
import com.zincoid.nullbot.core.service.file.FileService;
import com.zincoid.nullbot.core.util.MsgParseUtil;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@CommandMapping({"ImageSave", "保存图片"})
@Component
@RequiredArgsConstructor
public class ImageSaveCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        ArrayMsg reply = event.getArrayMsg().getFirst();
        if (reply.getType() != MsgTypeEnum.reply)
            throw new BotWarnException("缺少图片引用");
        MsgResp replyMsg = bot.getMsg(reply.getData().get("id").asInt()).getData();
        Map<String, String> imageMap = MsgParseUtil.extractImgMap(replyMsg.getRawMessage());
        if (imageMap.isEmpty())
            throw new BotWarnException("引用未包含图片");

        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();

        imageMap.forEach((name, url) -> {
            String key = name.substring(0, name.lastIndexOf("."));  // QQ图片扩展名错误
            String filePath = fileStorageProperties.getImagePath() + "/collect";
            FileInfo fileInfo = fileService.saveFile(url, filePath, key, userId, userName);
            bot.sendGroupMsg(groupId, "\uD83D\uDCBD图片已保存", false);
            log.info("☑ [ImageSave] 图片已保存: {}", fileInfo.getFileName());
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
