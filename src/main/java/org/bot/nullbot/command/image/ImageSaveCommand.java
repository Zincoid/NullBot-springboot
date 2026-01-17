package org.bot.nullbot.command.image;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GetMsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.info.FileInfo;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.FileService;
import org.bot.nullbot.util.DownloadUtil;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.stereotype.Component;

import java.util.Map;


@CommandMapping({"ImageSave", "保存图片"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ImageSaveCommand implements Command
{
    private final FileStorageConfig fileStorageConfig;
    private final FileService fileService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            ArrayMsg reply = groupMessageEvent.getArrayMsg().getFirst();
            if (reply.getType() != MsgTypeEnum.reply)
                throw new NullBotMsgException("[保存图片] ❌需引用图片");

            GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
            Map<String, String> imageMap = MessageParseUtil.parseGroupRawMessageAsImageMap(replyMsg.getRawMessage());
            if(imageMap.isEmpty())
                throw new NullBotMsgException("[保存图片] ❌未包含图片");

            Long userId = groupMessageEvent.getSender().getUserId();
            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
            Long groupId = groupMessageEvent.getGroupId();

            for (Map.Entry<String, String> entry : imageMap.entrySet()) {
                String originName = entry.getKey();
                String url = entry.getValue();
                // QQ给的扩展名是错的 让下载方法判断文件类型
                String fileName = originName.substring(0, originName.lastIndexOf("."));
                String filePath = fileStorageConfig.getImagePath() + "/collect";
                try {
                    FileInfo fileInfo = DownloadUtil.downloadFile(url, filePath, fileName, "\t\t\t\t├─ ");
                    if(!fileService.addFileRecordForBot(
                            filePath,
                            fileInfo.getFileName(),
                            fileInfo.getFileSize(),
                            fileInfo.getLastModified(),
                            userId, userName)
                    ) {
                        throw new NullBotMsgException("[保存图片] ❌数据库更新失败");
                    }
                    bot.sendGroupMsg(groupId, "\uD83D\uDCBD 已保存！", false);
                    log.info("\t\t\t\t├─[ImageSave] 已保存 - {}", fileInfo.getFileName());
                } catch (Exception e) {
                    throw new NullBotMsgException("[保存图片] ❌出错: " + e.getMessage());
                }
            }
        }else
            throw new NullBotLogException("[保存图片] ❌未设计 - 非群消息事件响应方式");
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
