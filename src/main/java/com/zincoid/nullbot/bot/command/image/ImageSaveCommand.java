package com.zincoid.nullbot.bot.command.image;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.model.information.FileInfo;
import com.zincoid.nullbot.core.service.FileService;
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
            throw new NullBotException("需引用图片");

        MsgResp replyMsg = bot.getMsg(reply.getData().get("id").asInt()).getData();
        Map<String, String> imageMap = MsgParseUtil.extractImgMap(replyMsg.getRawMessage());
        if (imageMap.isEmpty())
            throw new NullBotException("未包含图片");

        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();

        for (Map.Entry<String, String> entry : imageMap.entrySet()) {
            String originName = entry.getKey();
            String url = entry.getValue();
            // QQ给的扩展名是错的 让下载方法判断文件类型
            String fileName = originName.substring(0, originName.lastIndexOf("."));
            String filePath = fileStorageProperties.getImagePath() + "/collect";
            FileInfo fileInfo = fileService.saveFile(url, filePath, fileName, userId, userName);
            bot.sendGroupMsg(groupId, "\uD83D\uDCBD 已保存！", false);
            log.info("☑ [ImageSave] 图片已保存: {}", fileInfo.getFileName());
        }
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
